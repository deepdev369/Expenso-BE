package com.holytrinity.expenso.expense.adapter.in.web;

import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<ExpenseDTO> getExpense(@PathVariable(name = "expenseId") final String expenseId) {
        return ResponseEntity.ok(expenseUseCase.get(expenseId));
    }

    @PostMapping("/sync")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<List<ExpenseDTO>> syncExpenses(@RequestBody @Valid final List<ExpenseDTO> expenseDTOs) {
        return ResponseEntity.ok(expenseUseCase.processBulk(expenseDTOs));
    }

    @DeleteMapping("/sync")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteSyncExpenses(@RequestBody final List<String> expenseIds) {
        expenseUseCase.deleteBulk(expenseIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "202", description = "Accepted for processing")
    public ResponseEntity<Void> extractExpense(
            @org.springframework.web.bind.annotation.RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @org.springframework.web.bind.annotation.RequestParam(value = "text", required = false) String text,
            @org.springframework.web.bind.annotation.RequestParam(value = "expenseId", required = true) String expenseId) {
        expenseUseCase.submitForExtraction(file, text, expenseId);
        return ResponseEntity.accepted().build();
    }
}
