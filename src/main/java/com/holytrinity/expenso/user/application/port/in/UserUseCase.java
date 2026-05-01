package com.holytrinity.expenso.user.application.port.in;

import com.holytrinity.expenso.user.application.dto.UserDTO;

import java.util.List;

public interface UserUseCase {

    UserDTO getUser(String userId);

    UserDTO getUserByEmail(String email);

    List<UserDTO> findAllUsers();

    List<UserDTO> syncBulk(List<UserDTO> userDTOs);

    UserDTO createUserForRegistration(UserDTO userDTO);

    void deleteBulk(List<String> userIds);
}