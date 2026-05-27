package com.holytrinity.expenso.split.application.port.in;

import com.holytrinity.expenso.split.application.dto.SplitDTO;
import java.util.List;

public interface SplitUseCase {
    List<SplitDTO> processBulkSplits(String userId, List<SplitDTO> incomingSplits);
}
