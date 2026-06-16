package com.holytrinity.expenso.user.adapter.out.persistence;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAssociatedUserRepository extends JpaRepository<AssociatedUser, String> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM associated_user WHERE associated_user_id IN (:ids) AND user_id = :userId", nativeQuery = true)
    java.util.List<AssociatedUser> findAllWithDeletedByIdsAndUserId(@org.springframework.data.repository.query.Param("ids") java.util.List<String> ids, @org.springframework.data.repository.query.Param("userId") String userId);
}
