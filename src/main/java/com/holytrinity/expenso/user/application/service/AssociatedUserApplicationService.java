package com.holytrinity.expenso.user.application.service;

import com.holytrinity.expenso.user.application.dto.AssociatedUserDTO;
import com.holytrinity.expenso.user.application.port.in.AssociatedUserUseCase;
import com.holytrinity.expenso.user.application.port.out.AssociatedUserPort;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.AssociatedUser;
import com.holytrinity.expenso.user.domain.User;
import com.holytrinity.expenso.shared.exception.NotFoundException;
import com.holytrinity.expenso.security.UserContext;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AssociatedUserApplicationService implements AssociatedUserUseCase {

    private final AssociatedUserPort associatedUserPort;
    private final UserPort userPort;
    private final UserContext userContext;

    @Override
    public List<AssociatedUserDTO> syncBulk(List<AssociatedUserDTO> dtos) {
        log.info("Processing bulk associated users: {} items", dtos.size());
        Long currentUserId = userContext.getCurrentUserId();
        User currentUser = userPort.loadUser(currentUserId).orElseThrow(NotFoundException::new);

        return dtos.stream().map(dto -> {
            Optional<AssociatedUser> existing = associatedUserPort
                    .loadAssociatedUserByClientReferenceId(dto.getClientReferenceId());
            AssociatedUser entity = existing.orElseGet(AssociatedUser::new);

            entity.setClientReferenceId(dto.getClientReferenceId());
            entity.setName(dto.getName());
            entity.setPhone(dto.getPhone());
            entity.setEmail(dto.getEmail());
            entity.setUser(currentUser);

            if (dto.getVersion() != null) {
                entity.setVersion(dto.getVersion());
            }

            AssociatedUser saved = associatedUserPort.saveAssociatedUser(entity);
            return mapToDTO(saved);
        }).toList();
    }

    @Override
    public void deleteBulk(List<String> clientReferenceIds) {
        clientReferenceIds.forEach(id -> {
            associatedUserPort.loadAssociatedUserByClientReferenceId(id).ifPresent(entity -> {
                associatedUserPort.deleteAssociatedUser(entity);
            });
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssociatedUserDTO> findAll() {
        Long currentUserId = userContext.getCurrentUserId();
        return associatedUserPort.loadAllByUserId(currentUserId).stream()
                .map(this::mapToDTO).toList();
    }

    private AssociatedUserDTO mapToDTO(AssociatedUser entity) {
        AssociatedUserDTO dto = new AssociatedUserDTO();
        dto.setId(entity.getId());
        dto.setClientReferenceId(entity.getClientReferenceId());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setUserId(entity.getUser().getUserId());
        dto.setVersion(entity.getVersion());
        return dto;
    }
}
