package com.holytrinity.expenso.expense.application.service;

import com.holytrinity.expenso.events.BeforeDeleteUser;
import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import com.holytrinity.expenso.expense.application.port.out.ExpensePort;
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

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ExpenseDTO> findAll(org.springframework.data.domain.Pageable pageable) {
        Long userId = userContext.getCurrentUserId();
        return expensePort.findAllByUserUserId(userId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO get(Long expenseId) {
        Expense expense = expensePort.loadExpense(expenseId)
                .orElseThrow(NotFoundException::new);
        checkOwnership(expense);
        return mapToDTO(expense);
    }

    @Override
    public ExpenseDTO create(ExpenseDTO expenseDTO) {
        log.info("Creating expense for user: {}", expenseDTO.getUserID());
        Expense expense = new Expense();
        mapToEntity(expenseDTO, expense);
        Expense savedExpense = expensePort.saveExpense(expense);
        log.info("Expense created with ID: {}", savedExpense.getExpenseId());
        return mapToDTO(savedExpense);
    }

    @Override
    public ExpenseDTO update(Long expenseId, ExpenseDTO expenseDTO) {
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
    public void delete(Long expenseId) {
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
        return expenseDTOs.stream().map(dto -> {
            if (dto.getExpenseId() == null) {
                return create(dto);
            } else {
                return update(dto.getExpenseId(), dto);
            }
        }).toList();
    }

    @Override
    @Transactional
    public void deleteBulk(List<Long> expenseIds) {
        log.info("Processing bulk delete for {} items", expenseIds.size());
        expenseIds.forEach(this::delete);
    }

    @Override
    public List<ExpenseDTO> findAllByUserEmail(String email) {
        // Deprecated or Admin only. Enforcing check just in case.
        // Actually, strictly isolating means we shouldn't allow this unless admin.
        // For now, let's keep it but check if email matches current user?
        // Or if this method is not exposed in UseCase (it is).
        // Let's implement robust check.
        Long currentUserId = userContext.getCurrentUserId();
        // We'd need to load user by email to check ID, or just trust the port
        // constraint if strict.
        // Ideally we should remove this method from UseCase if not needed by API.
        // But for now, let's just delegate to port which *should* be safe if we only
        // use it for internal logic?
        // User requirements: "Pagination never leaks data".
        // Let's allow it but ensure it matches current user.
        // Actually simpler: This method was added by USER in previous turns.
        // Let's soft-deprecate it or wrap with check.
        return expensePort.findAllByUserEmail(email).stream()
                .filter(e -> e.getUser().getUserId().equals(currentUserId))
                .map(this::mapToDTO)
                .toList();
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final Expense userIDExpense = expensePort.findFirstByUserId(event.getUserId());
        if (userIDExpense != null) {
            referencedException.setKey("user.expense.userID.referenced");
            referencedException.addParam(userIDExpense.getExpenseId());
            throw referencedException;
        }
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

        if (expenseDTO.getUserID() != null) {
            // Ignore input userID, override with current context or validate match
        }
        // Always force current user
        Long currentUserId = userContext.getCurrentUserId();
        User user = userPort.loadUser(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + currentUserId));
        expense.setUser(user);
    }

    private void checkOwnership(Expense expense) {
        Long currentUserId = userContext.getCurrentUserId();
        if (!expense.getUser().getUserId().equals(currentUserId)) {
            throw new NotFoundException("Expense not found"); // Standard security practice to return 404
        }
    }
}
