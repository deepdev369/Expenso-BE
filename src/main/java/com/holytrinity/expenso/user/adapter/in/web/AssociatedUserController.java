package com.holytrinity.expenso.user.adapter.in.web;

import com.holytrinity.expenso.user.application.dto.AssociatedUserDTO;
import com.holytrinity.expenso.user.application.port.in.AssociatedUserUseCase;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/associated-user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AssociatedUserController {

    private final AssociatedUserUseCase useCase;

    @GetMapping
    public ResponseEntity<List<AssociatedUserDTO>> getAll() {
        return ResponseEntity.ok(useCase.findAll());
    }

    @PostMapping("/sync")
    public ResponseEntity<List<AssociatedUserDTO>> syncBulk(@RequestBody @Valid final List<AssociatedUserDTO> dtos) {
        return ResponseEntity.ok(useCase.syncBulk(dtos));
    }

    @DeleteMapping("/sync")
    public ResponseEntity<Void> deleteBulk(@RequestBody final List<String> clientReferenceIds) {
        useCase.deleteBulk(clientReferenceIds);
        return ResponseEntity.noContent().build();
    }
}
