package com.holytrinity.expenso.user.adapter.in.web;

import com.holytrinity.expenso.user.application.dto.UserDTO;
import com.holytrinity.expenso.user.application.port.in.UserUseCase;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userUseCase.findAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable(name = "userId") final Long userId) {
        return ResponseEntity.ok(userUseCase.getUser(userId));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid final UserDTO userDTO) {
        final UserDTO createdUser = userUseCase.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable(name = "userId") final Long userId,
            @RequestBody @Valid final UserDTO userDTO) {
        final UserDTO updatedUser = userUseCase.updateUser(userId, userDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "userId") final Long userId) {
        userUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
