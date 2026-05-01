package com.holytrinity.expenso.user.adapter.in.web;

import com.holytrinity.expenso.user.application.dto.UserDTO;
import com.holytrinity.expenso.user.application.port.in.UserUseCase;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;
    private final com.holytrinity.expenso.security.UserContext userContext;



    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUser() {
        return ResponseEntity.ok(userUseCase.getUser(userContext.getCurrentUserId()));
    }

    @PostMapping("/sync")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<UserDTO>> syncUsers(@RequestBody @Valid final List<UserDTO> userDTOs) {
        return ResponseEntity.ok(userUseCase.syncBulk(userDTOs));
    }

    @DeleteMapping("/sync")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteSyncUsers(@RequestBody final List<String> userIds) {
        userUseCase.deleteBulk(userIds);
        return ResponseEntity.noContent().build();
    }
}
