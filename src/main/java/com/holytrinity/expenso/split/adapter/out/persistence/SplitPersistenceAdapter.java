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
    public org.springframework.data.domain.Page<Split> findAll(org.springframework.data.domain.Pageable pageable) {
        return splitRepository.findAll(pageable);
    }

    @Override
    public java.util.Optional<Split> loadSplit(String splitId) {
        return splitRepository.findById(splitId);
    }

    @Override
    public void deleteSplit(Split split) {
        splitRepository.delete(split);
    }

    @Override
    public List<Split> saveAllSplits(List<Split> splits) {
        return splitRepository.saveAll(splits);
    }

    @Override
    public List<Split> findSplitsByUserId(String userId) {
        return splitRepository.findByCreatorUser_UserId(userId);
    }

    @Override
    public List<Split> findAllWithDeletedByUserId(String userId) {
        return splitRepository.findAllWithDeletedByUserId(userId);
    }

    @Override
    public List<Split> findAllWithDeletedByIdsAndUserId(List<String> ids, String userId) {
        return splitRepository.findAllWithDeletedByIdsAndUserId(ids, userId);
    }
}
