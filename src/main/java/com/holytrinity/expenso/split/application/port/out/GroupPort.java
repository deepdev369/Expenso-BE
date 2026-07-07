package com.holytrinity.expenso.split.application.port.out;

import com.holytrinity.expenso.split.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface GroupPort {
    Page<Group> findAll(Pageable pageable);
    Optional<Group> loadGroup(String id);
    List<Group> saveAllGroups(List<Group> groups);
}
