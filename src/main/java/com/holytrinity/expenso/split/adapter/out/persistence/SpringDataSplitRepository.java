package com.holytrinity.expenso.split.adapter.out.persistence;

import com.holytrinity.expenso.split.domain.Split;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataSplitRepository extends JpaRepository<Split, String> {
    List<Split> findByCreatorUser_UserId(String userId);
}
