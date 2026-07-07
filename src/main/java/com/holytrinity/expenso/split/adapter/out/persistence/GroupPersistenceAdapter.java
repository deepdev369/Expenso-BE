package com.holytrinity.expenso.split.adapter.out.persistence;

import com.holytrinity.expenso.split.application.port.out.GroupPort;
import com.holytrinity.expenso.split.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GroupPersistenceAdapter implements GroupPort {

    private final SpringDataGroupRepository repository;

    public GroupPersistenceAdapter(SpringDataGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Group> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Group> loadGroup(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Group> saveAllGroups(List<Group> groups) {
        return repository.saveAll(groups);
    }
}
