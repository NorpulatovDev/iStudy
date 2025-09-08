package com.ogabek.istudy.controller;

import com.ogabek.istudy.dto.request.CreateExpenseRequest;
import com.ogabek.istudy.dto.response.ExpenseDto;
import com.ogabek.istudy.security.BranchAccessControl;
import com.ogabek.istudy.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseController {
    
    private final ExpenseService expenseService;
    private final BranchAccessControl branchAccessControl;

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpensesByBranch(@RequestParam Long branchId) {
        if (!branchAccessControl.hasAccessToBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<ExpenseDto> expenses = expenseService.getExpensesByBranch(branchId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable Long id) {
        ExpenseDto expense = expenseService.getExpenseById(id);
        if (!branchAccessControl.hasAccessToBranch(expense.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(expense);
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        if (!branchAccessControl.hasAccessToBranch(request.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ExpenseDto expense = expenseService.createExpense(request);
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, 
                                                    @Valid @RequestBody CreateExpenseRequest request) {
        if (!branchAccessControl.hasAccessToBranch(request.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ExpenseDto expense = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        ExpenseDto expense = expenseService.getExpenseById(id);
        if (!branchAccessControl.hasAccessToBranch(expense.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        expenseService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }
}
