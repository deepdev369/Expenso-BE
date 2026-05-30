package com.holytrinity.expenso.chat.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.holytrinity.expenso.chat.application.dto.ChatRequestDTO;
import com.holytrinity.expenso.chat.application.dto.ChatResponseDTO;
import com.holytrinity.expenso.chat.application.port.in.ChatUseCase;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import com.holytrinity.expenso.expense.application.port.out.ExpensePort;
import com.holytrinity.expenso.plan.application.port.out.PlanPort;
import com.holytrinity.expenso.split.application.port.out.SplitPort;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ChatApplicationService implements ChatUseCase {

    @Value("${ai.service.base-url}")
    private String aiServiceBaseUrl;

    @Value("${ai.service.internal-token}")
    private String internalToken;

    private final ExpensePort expensePort;
    private final PlanPort planPort;
    private final SplitPort splitPort;
    private final UserPort userPort;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final ExpenseUseCase expenseUseCase;

    public ChatApplicationService(ExpensePort expensePort, PlanPort planPort, SplitPort splitPort,
                                  UserPort userPort, ObjectMapper objectMapper, RestClient.Builder restClientBuilder,
                                  ExpenseUseCase expenseUseCase) {
        this.expensePort = expensePort;
        this.planPort = planPort;
        this.splitPort = splitPort;
        this.userPort = userPort;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
        this.expenseUseCase = expenseUseCase;
    }

    @Override
    public ChatResponseDTO processChat(String userId, ChatRequestDTO requestDTO) {
        log.info("Processing chat prompt for user: {}", userId);
        
        try {
            // Aggregate user context — ALWAYS scoped to the requesting user
            var expenses = expensePort.findAllByUserId(userId, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "expenseDate"))).getContent();
            var goals = planPort.findGoalsByUserId(userId);
            var splits = splitPort.findSplitsByUserId(userId);
            var subscriptions = planPort.findSubscriptionsByUserId(userId);

            Map<String, Object> context = new HashMap<>();
            context.put("recent_expenses", expenses.stream().map(e -> Map.of(
                    "amount", e.getAmount(),
                    "category", e.getCategory(),
                    "date", e.getExpenseDate(),
                    "merchant", e.getMerchantName() != null ? e.getMerchantName() : ""
            )).toList());
            context.put("goals", goals.stream().map(g -> Map.of(
                    "name", g.getName(),
                    "current", g.getCurrentAmount(),
                    "target", g.getTargetAmount()
            )).toList());
            context.put("subscriptions", subscriptions.stream().map(s -> Map.of(
                    "name", s.getName(),
                    "amount", s.getAmount()
            )).toList());
            context.put("splits", splits.stream().map(s -> Map.of(
                    "description", s.getDescription(),
                    "amount", s.getTotalAmount()
            )).toList());

            String contextJson = objectMapper.writeValueAsString(context);

            // Construct payload for Python service
            Map<String, String> aiPayload = new HashMap<>();
            aiPayload.put("user_id", userId);
            aiPayload.put("prompt", requestDTO.getPrompt());
            aiPayload.put("context_json", contextJson);

            // Call Python AI Service
            String responseStr = restClient.post()
                    .uri(aiServiceBaseUrl + "/api/v1/chat")
                    .header("X-Internal-Token", internalToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(aiPayload)
                    .retrieve()
                    .body(String.class);

            var aiResponseNode = objectMapper.readTree(responseStr);
            if (aiResponseNode.path("success").asBoolean()) {
                String aiMessage = aiResponseNode.path("data").path("response").asText();
                String intent = aiResponseNode.path("data").path("intent").asText("chat");
                if ("extract_expense".equals(intent)) {
                    log.info("Detected extract_expense intent from AI chat. Submitting for extraction.");
                    expenseUseCase.submitForExtraction(null, requestDTO.getPrompt(), null);
                }
                return new ChatResponseDTO(aiMessage);
            } else {
                log.error("AI Service returned error: {}", responseStr);
                return new ChatResponseDTO("Sorry, I could not process your request at this moment.");
            }

        } catch (Exception e) {
            log.error("Error communicating with AI Service for chat", e);
            return new ChatResponseDTO("Sorry, there was an internal error connecting to the AI brain.");
        }
    }
}
