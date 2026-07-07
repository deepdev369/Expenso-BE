package com.holytrinity.expenso.split.application.port.in;

import com.holytrinity.expenso.split.application.dto.SplitDTO;
import com.holytrinity.expenso.split.application.dto.SplitExtractionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SplitUseCase {
    Page<SplitDTO> findAll(Pageable pageable);
    SplitDTO get(String splitId);
    SplitDTO update(String splitId, SplitDTO dto);
    void delete(String splitId);
    void deleteBulk(List<String> splitIds);
    List<SplitDTO> processBulkSplits(String userId, List<SplitDTO> incomingSplits);
    SplitDTO submitForExtraction(SplitExtractionRequest request);
}
