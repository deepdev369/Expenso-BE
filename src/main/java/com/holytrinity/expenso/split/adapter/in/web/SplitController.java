package com.holytrinity.expenso.split.adapter.in.web;

import com.holytrinity.expenso.split.application.dto.SplitDTO;
import com.holytrinity.expenso.split.application.port.in.SplitUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/splits")
public class SplitController {

    private final SplitUseCase splitUseCase;

    public SplitController(SplitUseCase splitUseCase) {
        this.splitUseCase = splitUseCase;
    }

    @PostMapping("/sync")
    public ResponseEntity<List<SplitDTO>> syncSplits(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<SplitDTO> incomingSplits) {
        String userId = jwt.getSubject();
        List<SplitDTO> updatedSplits = splitUseCase.processBulkSplits(userId, incomingSplits);
        return ResponseEntity.ok(updatedSplits);
    }
}
