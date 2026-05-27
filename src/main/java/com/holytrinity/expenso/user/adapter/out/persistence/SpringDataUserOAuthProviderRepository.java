package com.holytrinity.expenso.user.adapter.out.persistence;

import com.holytrinity.expenso.user.domain.UserOAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserOAuthProviderRepository extends JpaRepository<UserOAuthProvider, String> {
    Optional<UserOAuthProvider> findByProviderNameAndProviderSubjectId(String providerName, String providerSubjectId);
}
