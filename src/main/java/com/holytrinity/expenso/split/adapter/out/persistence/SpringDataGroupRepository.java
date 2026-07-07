package com.holytrinity.expenso.split.adapter.out.persistence;

import com.holytrinity.expenso.split.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataGroupRepository extends JpaRepository<Group, String> {
}
