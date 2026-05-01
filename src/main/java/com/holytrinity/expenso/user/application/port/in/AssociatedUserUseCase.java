package com.holytrinity.expenso.user.application.port.in;

import com.holytrinity.expenso.user.application.dto.AssociatedUserDTO;
import java.util.List;

public interface AssociatedUserUseCase {
    List<AssociatedUserDTO> syncBulk(List<AssociatedUserDTO> dtos);

    void deleteBulk(List<String> associatedUserIds);

    org.springframework.data.domain.Page<AssociatedUserDTO> findAll(org.springframework.data.domain.Pageable pageable);
}
