package com.holytrinity.expenso.chat.application.port.in;

import com.holytrinity.expenso.chat.application.dto.ChatRequestDTO;
import com.holytrinity.expenso.chat.application.dto.ChatResponseDTO;

public interface ChatUseCase {
    ChatResponseDTO processChat(String userId, ChatRequestDTO requestDTO);
}
