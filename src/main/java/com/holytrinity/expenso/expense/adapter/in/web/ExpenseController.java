package com.holytrinity.expenso.expense.adapter.in.web;

import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import com.holytrinity.expenso.expense.application.dto.ExpenseExtractionRequest;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
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
    public ResponseEntity<Page<ExpenseDTO>> getAllExpenses(
            @PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(expenseUseCase.findAll(pageable));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> getExpense(@PathVariable(name = "expenseId") final String expenseId) {
        return ResponseEntity.ok(expenseUseCase.get(expenseId));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable(name = "expenseId") final String expenseId, @RequestBody @Valid final ExpenseDTO expenseDTO) {
        return ResponseEntity.ok(expenseUseCase.update(expenseId, expenseDTO));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable(name = "expenseId") final String expenseId) {
        expenseUseCase.delete(expenseId);
        return ResponseEntity.noContent().build();
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
    @ApiResponse(responseCode = "200", description = "Extracted successfully")
    public ResponseEntity<List<ExpenseDTO>> extractExpense(
            @ModelAttribute @Valid final ExpenseExtractionRequest request) {
        List<ExpenseDTO> extractedExpenses = expenseUseCase.submitForExtraction(request);
        return ResponseEntity.ok(extractedExpenses);
    }
}
