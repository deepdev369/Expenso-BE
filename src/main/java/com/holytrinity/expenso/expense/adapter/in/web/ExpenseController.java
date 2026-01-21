package com.holytrinity.expenso.expense.adapter.in.web;

import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/expenses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseUseCase expenseUseCase;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<ExpenseDTO>> getAllExpenses(
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(expenseUseCase.findAll(pageable));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> getExpense(@PathVariable(name = "expenseId") final Long expenseId) {
        return ResponseEntity.ok(expenseUseCase.get(expenseId));
    }

    @PostMapping("/create")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody @Valid final ExpenseDTO expenseDTO) {
        final ExpenseDTO createdExpense = expenseUseCase.create(expenseDTO);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    @PutMapping("/update/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable(name = "expenseId") final Long expenseId,
            @RequestBody @Valid final ExpenseDTO expenseDTO) {
        final ExpenseDTO updatedExpense = expenseUseCase.update(expenseId, expenseDTO);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/delete/{expenseId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteExpense(@PathVariable(name = "expenseId") final Long expenseId) {
        expenseUseCase.delete(expenseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-bulk")
    public ResponseEntity<List<ExpenseDTO>> createBulkExpenses(@RequestBody @Valid final List<ExpenseDTO> expenseDTOs) {
        // Simple heuristic: if ID is null, create. If ID is present, update.
        // However, standard REST usually separates these or uses PATCH.
        // User requested "Single bulk API that can Create new... Update existing".
        // I will split them in the service or just call one method if the controller
        // receives mixed.
        // But let's check the UseCase. I added createBulk and updateBulk.
        // Let's implement a "processBulk" or similar?
        // Actually, for simplicity and clarity, let's expose specific bulk endpoints or
        // logic to split them.
        // User asked: "3.1 Bulk Create & Update... Implement a single bulk API".
        // So I'll implement `POST /bulk` and delegate to Service to handle both.
        // But I defined `createBulk` and `updateBulk` in the interface.
        // I should probably correct the Interface to `processBulk` or handle it in
        // Controller.
        // Let's handle it in Controller by splitting the list? No, transactional
        // boundary should be in Service.
        // I'll update the Interface to `List<ExpenseDTO> saveBulk(List<ExpenseDTO>
        // expenses)` or similar.
        // For now, let's assume I'll call `createBulk` for the ones without ID and
        // `updateBulk` for ones with ID.
        // But that breaks transactionality if done in Controller.
        // So I will change the Plan slightly to add `processBulk` or similar.
        // Actually, let's just stick to the plan: "Implement a single bulk API...
        // Create new... Update existing".
        // I'll assume the Controller receives a list.

        // Refactoring on the fly: I'll use `createBulk` for strictly new, `updateBulk`
        // for existing.
        // But the user wants a SINGLE endpoint.
        // I'll use `POST /bulk` and inside I will pass the whole list to a new method
        // `saveBulk` in Service.
        // So I need to update the use case again?
        // Or I can just call `processBulk` in the controller.

        return ResponseEntity.ok(expenseUseCase.processBulk(expenseDTOs));
    }

    @DeleteMapping("/delete-bulk")
    public ResponseEntity<Void> deleteBulkExpenses(@RequestBody final List<Long> expenseIds) {
        expenseUseCase.deleteBulk(expenseIds);
        return ResponseEntity.noContent().build();
    }
}
