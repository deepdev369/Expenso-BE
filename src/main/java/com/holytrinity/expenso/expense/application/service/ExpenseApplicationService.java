package com.holytrinity.expenso.expense.application.service;

import com.holytrinity.expenso.events.BeforeDeleteUser;
import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import com.holytrinity.expenso.expense.application.port.out.ExpensePort;
import com.holytrinity.expenso.expense.application.port.out.dto.AiExtractionRequest;
import com.holytrinity.expenso.expense.domain.Expense;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.User;
import com.holytrinity.expenso.shared.exception.NotFoundException;
import com.holytrinity.expenso.shared.exception.ReferencedException;
import org.springframework.context.ApplicationEventPublisher;
import com.holytrinity.expenso.events.BeforeDeleteExpense;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseApplicationService implements ExpenseUseCase {

    private final ExpensePort expensePort;
    private final UserPort userPort;
    private final ApplicationEventPublisher publisher;
    private final com.holytrinity.expenso.security.UserContext userContext;
    private final com.holytrinity.expenso.expense.application.port.out.AiExtractionPort aiExtractionPort;

    @org.springframework.beans.factory.annotation.Value("${app.webhook.base-url}")
    private String webhookBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ExpenseDTO> findAll(org.springframework.data.domain.Pageable pageable) {
        return expensePort.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO get(String expenseId) {
        Expense expense = expensePort.loadExpense(expenseId)
                .orElseThrow(NotFoundException::new);
        checkOwnership(expense);
        return mapToDTO(expense);
    }

    private ExpenseDTO create(ExpenseDTO expenseDTO) {
        log.info("Creating expense for user: {}", expenseDTO.getUserID());
        Expense expense = new Expense();
        mapToEntity(expenseDTO, expense);
        Expense savedExpense = expensePort.saveExpense(expense);
        log.info("Expense created with ID: {}", savedExpense.getExpenseId());
        return mapToDTO(savedExpense);
    }

    @Override
    @Transactional
    public ExpenseDTO update(String expenseId, ExpenseDTO expenseDTO) {
        log.info("Updating expense with ID: {}", expenseId);
        Expense expense = expensePort.loadExpense(expenseId)
                .orElseThrow(NotFoundException::new);
        checkOwnership(expense);
        mapToEntity(expenseDTO, expense);
        Expense updatedExpense = expensePort.saveExpense(expense);
        log.info("Expense updated: {}", expenseId);
        return mapToDTO(updatedExpense);
    }

    @Override
    @Transactional
    public void delete(String expenseId) {
        log.info("Deleting expense with ID: {}", expenseId);
        Expense expense = expensePort.loadExpense(expenseId)
                .orElseThrow(NotFoundException::new);
        checkOwnership(expense);
        publisher.publishEvent(new BeforeDeleteExpense(expenseId));
        expense.setDeleted(true);
        expensePort.saveExpense(expense);
        log.info("Expense soft-deleted: {}", expenseId);
    }

    @Override
    @Transactional
    public List<ExpenseDTO> processBulk(List<ExpenseDTO> expenseDTOs) {
        log.info("Processing bulk expenses: {} items", expenseDTOs.size());
        String currentUserId = userContext.getCurrentUserId();
        
        List<String> incomingIds = expenseDTOs.stream().map(ExpenseDTO::getExpenseId).toList();
        List<Expense> existingExpenses = incomingIds.isEmpty() ? new java.util.ArrayList<>() : expensePort.findAllWithDeletedByIdsAndUserId(incomingIds, currentUserId);
        java.util.Map<String, Expense> existingExpenseMap = existingExpenses.stream()
                .collect(java.util.stream.Collectors.toMap(Expense::getExpenseId, e -> e));

        List<ExpenseDTO> results = new java.util.ArrayList<>();
        for (ExpenseDTO dto : expenseDTOs) {
            Expense existing = existingExpenseMap.get(dto.getExpenseId());
            if (existing == null) {
                // Must ensure the DTO has the user ID so create() uses it correctly
                dto.setUserID(currentUserId);
                results.add(create(dto));
            } else {
                if (dto.getVersion() != null && dto.getVersion() < existing.getVersion()) {
                    log.info("Ignoring stale update from client for expense {}, server version: {}, client version: {}", 
                             existing.getExpenseId(), existing.getVersion(), dto.getVersion());
                    results.add(mapToDTO(existing));
                } else {
                    dto.setUserID(currentUserId);
                    results.add(update(existing.getExpenseId(), dto));
                }
            }
        }
        return results;
    }

    @Override
    @Transactional
    public void deleteBulk(List<String> expenseIds) {
        log.info("Processing bulk delete for {} items", expenseIds.size());
        expenseIds.forEach(id -> {
            expensePort.loadExpense(id).ifPresent(expense -> {
                delete(expense.getExpenseId());
            });
        });
    }



    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        if (expensePort.existsByUserId(event.getUserId())) {
            final ReferencedException referencedException = new ReferencedException();
            referencedException.setKey("user.expense.userID.referenced");
            referencedException.addParam(event.getUserId());
            throw referencedException;
        }
    }

    @Override
    public ExpenseDTO submitForExtraction(org.springframework.web.multipart.MultipartFile file, String text,
            String expenseId) {
        String currentUserId = userContext.getCurrentUserId();
        User currentUser = userPort.loadUser(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (expenseId != null && !expenseId.trim().isEmpty()) {
            Expense existing = expensePort.loadExpense(expenseId)
                    .orElseThrow(() -> new NotFoundException("Expense not found"));
            checkOwnership(existing);
        }
        AiExtractionRequest request = AiExtractionRequest
                .builder()
                .userId(currentUserId)
                .expenseId(expenseId)
                .rawText(text)
                .file(file)
                .currency(currentUser.getDefaultCurrency())
                .userLanguage(currentUser.getLanguage())
                .categoriesMapping(currentUser.getCategoriesMapping())
                .paymentMethods(currentUser.getPaymentMethods())
                .build();
        
        com.fasterxml.jackson.databind.JsonNode payload = aiExtractionPort.submitExpenseForExtraction(request);
        return processAiExtractionResult(payload, currentUserId, expenseId);
    }

    private ExpenseDTO processAiExtractionResult(com.fasterxml.jackson.databind.JsonNode payload, String currentUserId, String providedExpenseId) {
        log.info("Received AI Extraction Result. Status: {}", payload.path("status").asText());

        String status = payload.path("status").asText("");
        boolean success = "COMPLETED".equals(status);

        if (!success) {
            log.error("AI Microservice reported extraction failure: {}", payload.path("error").asText());
            if (providedExpenseId != null && !providedExpenseId.isBlank()) {
                expensePort.loadExpense(providedExpenseId).ifPresent(expense -> {
                    expense.setStatus("FAILED_EXTRACTION");
                    expensePort.saveExpense(expense);
                });
            }
            throw new RuntimeException("AI Extraction failed: " + payload.path("error").asText());
        }

        com.fasterxml.jackson.databind.JsonNode result = payload.path("result");
        com.fasterxml.jackson.databind.JsonNode extractedList = result.path("extracted_data");

        if (!extractedList.isArray() || extractedList.size() == 0) {
            throw new RuntimeException("AI extraction returned COMPLETED but no data was found");
        }

        com.fasterxml.jackson.databind.JsonNode data = extractedList.get(0);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setAmount(data.path("amount").asDouble(0.0));
        dto.setCategory(data.path("category").asText(null));
        dto.setSubCategory(data.path("sub_category").asText(null));

        String paymentMethod = data.path("payment_method").asText(null);
        dto.setPaymentMode(paymentMethod);

        String merchant = data.path("merchant").asText(null);
        dto.setMerchantName(merchant);

        boolean isDebit = data.path("is_debit").asBoolean(true);
        dto.setTransactionType(isDebit ? "DEBIT" : "CREDIT");

        if (data.path("date").isTextual() && !data.path("date").asText().isBlank()) {
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(data.path("date").asText());
                dto.setExpenseDate(ld.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli());
            } catch (Exception e) {
                dto.setExpenseDate(System.currentTimeMillis());
            }
        } else if (data.path("date").isNumber()) {
            dto.setExpenseDate(data.path("date").asLong(System.currentTimeMillis()));
        } else {
            dto.setExpenseDate(System.currentTimeMillis());
        }

        dto.setStatus("PROCESSED_BY_AI");
        dto.setUserID(currentUserId);
        
        String expenseId = payload.path("expenseId").asText(null);
        if (expenseId == null || expenseId.isBlank()) {
            expenseId = providedExpenseId;
        }
        if (expenseId != null && expenseId.isBlank()) expenseId = null;
        dto.setExpenseId(expenseId);

        return saveExtractedExpense(dto, currentUserId);
    }

    private ExpenseDTO saveExtractedExpense(ExpenseDTO expenseDTO, String assignedUserId) {
        log.info("Saving AI expense for user: {}", assignedUserId);

        java.util.Optional<Expense> existingOpt = expenseDTO.getExpenseId() != null
            ? expensePort.loadExpense(expenseDTO.getExpenseId())
            : java.util.Optional.empty();

        Expense expense;
        if (existingOpt.isPresent()) {
            expense = existingOpt.get();
            if (!expense.getUser().getUserId().equals(assignedUserId)) {
                throw new org.springframework.security.access.AccessDeniedException("Cannot overwrite another user's expense");
            }
        } else {
            expense = new Expense();
            expense.setExpenseId(expenseDTO.getExpenseId() != null ? expenseDTO.getExpenseId() : java.util.UUID.randomUUID().toString());
        }

        expense.setAmount(expenseDTO.getAmount());
        expense.setCategory(expenseDTO.getCategory());
        expense.setSubCategory(expenseDTO.getSubCategory());
        expense.setPaymentMode(expenseDTO.getPaymentMode());
        expense.setTransactionType(expenseDTO.getTransactionType());
        expense.setMerchantName(expenseDTO.getMerchantName());
        expense.setRawText(expenseDTO.getRawText());
        expense.setStatus(expenseDTO.getStatus());
        expense.setExpenseDate(expenseDTO.getExpenseDate());
        expense.setUserConfirmed(false);
        expense.setSource("AI");

        User user = userPort.loadUser(assignedUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + assignedUserId));
        expense.setUser(user);

        Expense saved = expensePort.saveExpense(expense);
        return mapToDTO(saved);
    }

    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setExpenseId(expense.getExpenseId());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setCategory(expense.getCategory());
        expenseDTO.setSubCategory(expense.getSubCategory());
        expenseDTO.setTags(expense.getTags());
        expenseDTO.setPaymentMode(expense.getPaymentMode());
        expenseDTO.setTransactionType(expense.getTransactionType());
        expenseDTO.setMerchantName(expense.getMerchantName());
        expenseDTO.setSource(expense.getSource());
        expenseDTO.setUserConfirmed(expense.getUserConfirmed());
        expenseDTO.setRawText(expense.getRawText());
        expenseDTO.setNormalizedText(expense.getNormalizedText());
        expenseDTO.setStatus(expense.getStatus());
        expenseDTO.setExpenseDate(expense.getExpenseDate());
        expenseDTO.setUserID(expense.getUser() != null ? expense.getUser().getUserId() : null);
        expenseDTO.setVersion(expense.getVersion());
        return expenseDTO;
    }

    private void mapToEntity(ExpenseDTO expenseDTO, Expense expense) {
        expense.setExpenseId(expenseDTO.getExpenseId());
        expense.setAmount(expenseDTO.getAmount());
        expense.setCategory(expenseDTO.getCategory());
        expense.setSubCategory(expenseDTO.getSubCategory());
        expense.setTags(expenseDTO.getTags());
        expense.setPaymentMode(expenseDTO.getPaymentMode());
        expense.setTransactionType(expenseDTO.getTransactionType());
        expense.setMerchantName(expenseDTO.getMerchantName());
        expense.setSource(expenseDTO.getSource());
        expense.setUserConfirmed(expenseDTO.getUserConfirmed());
        expense.setRawText(expenseDTO.getRawText());
        expense.setNormalizedText(expenseDTO.getNormalizedText());
        expense.setStatus(expenseDTO.getStatus());
        expense.setExpenseDate(expenseDTO.getExpenseDate());
        if (expenseDTO.getVersion() != null) {
            expense.setVersion(expenseDTO.getVersion());
        }
        // Always force the current authenticated user — ignore any client-provided userId
        String currentUserId = userContext.getCurrentUserId();
        User user = userPort.loadUser(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + currentUserId));
        expense.setUser(user);
    }

    private void checkOwnership(Expense expense) {
        String currentUserId = userContext.getCurrentUserId();
        if (!expense.getUser().getUserId().equals(currentUserId)) {
            throw new NotFoundException("Expense not found"); // Standard security practice to return 404
        }
    }
}
