package com.holytrinity.expenso.split.adapter.out.persistence;

import com.holytrinity.expenso.split.domain.Split;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataSplitRepository extends JpaRepository<Split, String> {
    List<Split> findByCreatorUser_UserId(String userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM splits WHERE user_id = :userId", nativeQuery = true)
    List<Split> findAllWithDeletedByUserId(@org.springframework.data.repository.query.Param("userId") String userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM splits WHERE split_id IN (:ids) AND user_id = :userId", nativeQuery = true)
    List<Split> findAllWithDeletedByIdsAndUserId(@org.springframework.data.repository.query.Param("ids") List<String> ids, @org.springframework.data.repository.query.Param("userId") String userId);
}
