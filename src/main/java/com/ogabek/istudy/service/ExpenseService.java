package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateExpenseRequest;
import com.ogabek.istudy.dto.response.ExpenseDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Expense;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BranchRepository branchRepository;

    public List<ExpenseDto> getExpensesByBranch(Long branchId) {
        return expenseRepository.findByBranchId(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ExpenseDto getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        return convertToDto(expense);
    }

    public ExpenseDto createExpense(CreateExpenseRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setBranch(branch);

        Expense savedExpense = expenseRepository.save(expense);
        return convertToDto(savedExpense);
    }

    public ExpenseDto updateExpense(Long id, CreateExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setBranch(branch);

        Expense savedExpense = expenseRepository.save(expense);
        return convertToDto(savedExpense);
    }

    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    private ExpenseDto convertToDto(Expense expense) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory().name());
        dto.setBranchId(expense.getBranch().getId());
        dto.setBranchName(expense.getBranch().getName());
        dto.setCreatedAt(expense.getCreatedAt());
        return dto;
    }
}
