package com.holytrinity.expenso.user.application.port.in;

import com.holytrinity.expenso.user.application.dto.UserDTO;

import java.util.List;

public interface UserUseCase {
    UserDTO createUser(UserDTO userDTO);

    UserDTO getUser(Long userId);

    List<UserDTO> findAllUsers();

    UserDTO updateUser(Long userId, UserDTO userDTO);

    void deleteUser(Long userId);
}