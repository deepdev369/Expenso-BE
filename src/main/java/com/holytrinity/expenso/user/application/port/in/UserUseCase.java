package com.holytrinity.expenso.user.application.port.in;

import com.holytrinity.expenso.user.application.dto.UserDTO;

import java.util.List;

public interface UserUseCase {

    UserDTO getUser(Long userId);

    List<UserDTO> findAllUsers();

    List<UserDTO> syncBulk(List<UserDTO> userDTOs);

    void deleteBulk(List<String> clientReferenceIds);
}