package com.holytrinity.expenso.user.adapter.out.persistence;

import com.holytrinity.expenso.user.application.port.out.AssociatedUserPort;
import com.holytrinity.expenso.user.domain.AssociatedUser;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssociatedUserPersistenceAdapter implements AssociatedUserPort {

    private final SpringDataAssociatedUserRepository repository;

    @Override
    public Optional<AssociatedUser> loadAssociatedUser(String associatedUserId) {
        return repository.findById(associatedUserId);
    }

    @Override
    public org.springframework.data.domain.Page<AssociatedUser> loadAll(
            org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public AssociatedUser saveAssociatedUser(AssociatedUser associatedUser) {
        return repository.save(associatedUser);
    }

    @Override
    public void deleteAssociatedUser(AssociatedUser associatedUser) {
        repository.delete(associatedUser);
    }
}
