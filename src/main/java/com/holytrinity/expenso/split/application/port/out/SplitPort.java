package com.holytrinity.expenso.split.application.port.out;

import com.holytrinity.expenso.split.domain.Split;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SplitPort {
    Page<Split> findAll(Pageable pageable);
    Optional<Split> loadSplit(String splitId);
    void deleteSplit(Split split);
    List<Split> saveAllSplits(List<Split> splits);
    List<Split> findSplitsByUserId(String userId);
    List<Split> findAllWithDeletedByUserId(String userId);
    List<Split> findAllWithDeletedByIdsAndUserId(List<String> ids, String userId);
}
