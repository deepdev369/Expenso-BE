package com.holytrinity.expenso.split.adapter.out.persistence;

import com.holytrinity.expenso.split.application.port.out.SplitPort;
import com.holytrinity.expenso.split.domain.Split;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SplitPersistenceAdapter implements SplitPort {

    private final SpringDataSplitRepository splitRepository;

    public SplitPersistenceAdapter(SpringDataSplitRepository splitRepository) {
        this.splitRepository = splitRepository;
    }

    @Override
    public List<Split> saveAllSplits(List<Split> splits) {
        return splitRepository.saveAll(splits);
    }

    @Override
    public List<Split> findSplitsByUserId(String userId) {
        return splitRepository.findByCreatorUser_UserId(userId);
    }
}
