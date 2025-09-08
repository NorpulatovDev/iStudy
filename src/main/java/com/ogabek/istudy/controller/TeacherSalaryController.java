package com.ogabek.istudy.controller;

import com.ogabek.istudy.dto.request.SalaryCalculationRequest;
import com.ogabek.istudy.dto.response.TeacherSalaryCalculationDto;
import com.ogabek.istudy.security.BranchAccessControl;
import com.ogabek.istudy.service.TeacherSalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TeacherSalaryController {
    
    private final TeacherSalaryService teacherSalaryService;
    private final BranchAccessControl branchAccessControl;

    @PostMapping("/calculate/branch/{branchId}")
    public ResponseEntity<List<TeacherSalaryCalculationDto>> calculateSalariesForBranch(
            @PathVariable Long branchId,
            @Valid @RequestBody SalaryCalculationRequest request) {
        
        if (!branchAccessControl.hasAccessToBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<TeacherSalaryCalculationDto> salaries = teacherSalaryService
                .calculateSalariesForBranch(branchId, request);
        return ResponseEntity.ok(salaries);
    }

    @PostMapping("/calculate/teacher/{teacherId}")
    public ResponseEntity<TeacherSalaryCalculationDto> calculateTeacherSalary(
            @PathVariable Long teacherId,
            @Valid @RequestBody SalaryCalculationRequest request) {
        
        TeacherSalaryCalculationDto salary = teacherSalaryService
                .calculateTeacherSalary(teacherId, request);
        
        if (!branchAccessControl.hasAccessToBranch(salary.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(salary);
    }

    @GetMapping("/history/branch/{branchId}")
    public ResponseEntity<List<TeacherSalaryCalculationDto>> getSalaryHistory(
            @PathVariable Long branchId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        if (!branchAccessControl.hasAccessToBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<TeacherSalaryCalculationDto> salaries = teacherSalaryService
                .getSalaryHistory(branchId, year, month);
        return ResponseEntity.ok(salaries);
    }

    @GetMapping("/history/teacher/{teacherId}")
    public ResponseEntity<List<TeacherSalaryCalculationDto>> getTeacherSalaryHistory(
            @PathVariable Long teacherId) {
        
        List<TeacherSalaryCalculationDto> salaries = teacherSalaryService
                .getTeacherSalaryHistory(teacherId);
        
        // Check access to first salary record's branch
        if (!salaries.isEmpty() && !branchAccessControl.hasAccessToBranch(salaries.get(0).getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(salaries);
    }

    @PutMapping("/{calculationId}/mark-paid")
    public ResponseEntity<TeacherSalaryCalculationDto> markSalaryAsPaid(@PathVariable Long calculationId) {
        TeacherSalaryCalculationDto salary = teacherSalaryService.markSalaryAsPaid(calculationId);
        
        if (!branchAccessControl.hasAccessToBranch(salary.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(salary);
    }
}