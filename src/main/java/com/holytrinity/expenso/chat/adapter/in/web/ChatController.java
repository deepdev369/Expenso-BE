package com.holytrinity.expenso.chat.adapter.in.web;

import com.holytrinity.expenso.chat.application.dto.ChatRequestDTO;
import com.holytrinity.expenso.chat.application.dto.ChatResponseDTO;
import com.holytrinity.expenso.chat.application.port.in.ChatUseCase;
import com.holytrinity.expenso.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final UserContext userContext;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponseDTO> processChat(@RequestBody ChatRequestDTO request) {
        String currentUserId = userContext.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(chatUseCase.processChat(currentUserId, request));
    }
}
