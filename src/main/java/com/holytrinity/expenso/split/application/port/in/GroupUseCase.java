package com.holytrinity.expenso.split.application.port.in;

import com.holytrinity.expenso.split.application.dto.GroupDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface GroupUseCase {
    Page<GroupDTO> findAll(Pageable pageable);
    GroupDTO get(String id);
    GroupDTO update(String id, GroupDTO dto);
    void delete(String id);
}
