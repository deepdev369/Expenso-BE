package com.holytrinity.expenso.split.adapter.out.persistence;

import com.holytrinity.expenso.split.domain.SplitMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSplitMemberRepository extends JpaRepository<SplitMember, String> {
}
