package com.holytrinity.expenso.user.application.service;

import com.holytrinity.expenso.user.application.dto.UserDTO;
import com.holytrinity.expenso.user.application.port.in.UserUseCase;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.User;
import com.holytrinity.expenso.events.BeforeDeleteUser;
import com.holytrinity.expenso.shared.exception.NotFoundException;
import com.holytrinity.expenso.shared.exception.ResourceAlreadyExistsException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService implements UserUseCase {

    private final UserPort userPort;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        return userPort.loadAllUsers().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(Long userId) {
        return userPort.loadUser(userId)
                .map(this::mapToDTO)
                .orElseThrow(NotFoundException::new);
    }

    private UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with email: {}", userDTO.getEmail());
        if (userPort.existsByEmail(userDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("User with email " + userDTO.getEmail() + " already exists");
        }
        User user = new User();
        mapToEntity(userDTO, user);
        User savedUser = userPort.saveUser(user);
        log.info("User created with ID: {}", savedUser.getUserId());
        return mapToDTO(savedUser);
    }

    private UserDTO updateUser(Long userId, UserDTO userDTO) {
        log.info("Updating user with ID: {}", userId);
        User user = userPort.loadUser(userId)
                .orElseThrow(NotFoundException::new);
        mapToEntity(userDTO, user);
        User updatedUser = userPort.saveUser(user);
        log.info("User updated: {}", userId);
        return mapToDTO(updatedUser);
    }

    private void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        User user = userPort.loadUser(userId)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteUser(userId));
        userPort.deleteUser(user);
        log.info("User deleted: {}", userId);
    }

    @Override
    @Transactional
    public List<UserDTO> syncBulk(List<UserDTO> userDTOs) {
        log.info("Processing bulk users: {} items", userDTOs.size());
        return userDTOs.stream().map(dto -> {
            java.util.Optional<User> existing = userPort.loadUserByClientReferenceId(dto.getClientReferenceId());
            if (existing.isEmpty()) {
                return createUser(dto);
            } else {
                return updateUser(existing.get().getUserId(), dto);
            }
        }).toList();
    }

    @Override
    @Transactional
    public void deleteBulk(List<String> clientReferenceIds) {
        log.info("Processing bulk user delete for {} items", clientReferenceIds.size());
        clientReferenceIds.forEach(id -> {
            userPort.loadUserByClientReferenceId(id).ifPresent(user -> {
                deleteUser(user.getUserId());
            });
        });
    }

    private UserDTO mapToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setClientReferenceId(user.getClientReferenceId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUserName(user.getUserName());
        userDTO.setPhone(user.getPhone());
        userDTO.setAuthProviders(user.getAuthProviders());
        userDTO.setPasswordHash(user.getPasswordHash());
        userDTO.setEmailVerified(user.getEmailVerified());
        userDTO.setDefaultCurrency(user.getDefaultCurrency());
        userDTO.setLanguage(user.getLanguage());
        userDTO.setCategoriesMapping(user.getCategoriesMapping());
        userDTO.setPaymentMethods(user.getPaymentMethods());
        userDTO.setSmsConsentGranted(user.getSmsConsentGranted());
        userDTO.setVoiceConsentGranted(user.getVoiceConsentGranted());
        userDTO.setConsentGrantedAt(user.getConsentGrantedAt());
        userDTO.setLastLoginAt(user.getLastLoginAt());
        return userDTO;
    }

    private void mapToEntity(UserDTO userDTO, User user) {
        user.setClientReferenceId(userDTO.getClientReferenceId());
        user.setEmail(userDTO.getEmail());
        user.setUserName(userDTO.getUserName());
        user.setPhone(userDTO.getPhone());
        user.setAuthProviders(userDTO.getAuthProviders());
        user.setPasswordHash(userDTO.getPasswordHash());
        user.setEmailVerified(userDTO.getEmailVerified());
        user.setDefaultCurrency(userDTO.getDefaultCurrency());
        user.setLanguage(userDTO.getLanguage());
        user.setCategoriesMapping(userDTO.getCategoriesMapping());
        user.setPaymentMethods(userDTO.getPaymentMethods());
        user.setSmsConsentGranted(userDTO.getSmsConsentGranted());
        user.setVoiceConsentGranted(userDTO.getVoiceConsentGranted());
        user.setConsentGrantedAt(userDTO.getConsentGrantedAt());
        user.setLastLoginAt(userDTO.getLastLoginAt());
    }
}
