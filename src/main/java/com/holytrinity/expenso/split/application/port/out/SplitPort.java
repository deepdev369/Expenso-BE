package com.holytrinity.expenso.split.application.port.out;

import com.holytrinity.expenso.split.domain.Split;
import java.util.List;

public interface SplitPort {
    List<Split> saveAllSplits(List<Split> splits);
    List<Split> findSplitsByUserId(String userId);
}
