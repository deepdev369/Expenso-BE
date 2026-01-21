package com.holytrinity.expenso.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BeforeDeleteExpense {
    private final Long expenseId;
}
