package com.holytrinity.expenso.split.adapter.in.web;

import com.holytrinity.expenso.split.application.dto.SplitDTO;
import com.holytrinity.expenso.split.application.dto.SplitExtractionRequest;
import com.holytrinity.expenso.split.application.port.in.SplitUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/splits")
public class SplitController {

    private final SplitUseCase splitUseCase;
    private final com.holytrinity.expenso.security.UserContext userContext;

    public SplitController(SplitUseCase splitUseCase, com.holytrinity.expenso.security.UserContext userContext) {
        this.splitUseCase = splitUseCase;
        this.userContext = userContext;
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<SplitDTO>> getAll(
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(splitUseCase.findAll(pageable));
    }

    @org.springframework.web.bind.annotation.GetMapping("/{splitId}")
    public ResponseEntity<SplitDTO> getSplit(@org.springframework.web.bind.annotation.PathVariable(name = "splitId") final String splitId) {
        return ResponseEntity.ok(splitUseCase.get(splitId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{splitId}")
    public ResponseEntity<SplitDTO> updateSplit(
            @org.springframework.web.bind.annotation.PathVariable(name = "splitId") final String splitId, 
            @RequestBody @jakarta.validation.Valid final SplitDTO splitDTO) {
        return ResponseEntity.ok(splitUseCase.update(splitId, splitDTO));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{splitId}")
    public ResponseEntity<Void> deleteSplit(@org.springframework.web.bind.annotation.PathVariable(name = "splitId") final String splitId) {
        splitUseCase.delete(splitId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<List<SplitDTO>> syncSplits(
            @RequestBody @jakarta.validation.Valid List<SplitDTO> incomingSplits) {
        String userId = userContext.getCurrentUserId();
        List<SplitDTO> updatedSplits = splitUseCase.processBulkSplits(userId, incomingSplits);
        return ResponseEntity.ok(updatedSplits);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/sync")
    public ResponseEntity<Void> deleteBulk(@RequestBody final List<String> splitIds) {
        splitUseCase.deleteBulk(splitIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SplitDTO> extractSplit(
            @ModelAttribute @jakarta.validation.Valid final SplitExtractionRequest request) {
        SplitDTO extractedSplit = splitUseCase.submitForExtraction(request);
        return ResponseEntity.ok(extractedSplit);
    }
}
