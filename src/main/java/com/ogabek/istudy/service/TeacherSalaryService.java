package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.SalaryCalculationRequest;
import com.ogabek.istudy.dto.response.TeacherSalaryCalculationDto;
import com.ogabek.istudy.entity.*;
import com.ogabek.istudy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherSalaryService {
    private final TeacherSalaryCalculationRepository salaryCalculationRepository;
    private final TeacherRepository teacherRepository;
    private final PaymentRepository paymentRepository;

    public List<TeacherSalaryCalculationDto> calculateSalariesForBranch(Long branchId, SalaryCalculationRequest request) {
        List<Teacher> teachers = teacherRepository.findByBranchId(branchId);
        
        return teachers.stream()
                .map(teacher -> calculateTeacherSalary(teacher, request.getYear(), request.getMonth()))
                .collect(Collectors.toList());
    }

    public TeacherSalaryCalculationDto calculateTeacherSalary(Long teacherId, SalaryCalculationRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        
        return calculateTeacherSalary(teacher, request.getYear(), request.getMonth());
    }

    public List<TeacherSalaryCalculationDto> getSalaryHistory(Long branchId, Integer year, Integer month) {
        return salaryCalculationRepository.findByBranchIdAndYearAndMonth(branchId, year, month)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TeacherSalaryCalculationDto> getTeacherSalaryHistory(Long teacherId) {
        return salaryCalculationRepository.findByTeacherId(teacherId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TeacherSalaryCalculationDto calculateTeacherSalary(Teacher teacher, int year, int month) {
        // Check if salary already calculated for this month
        var existingCalculation = salaryCalculationRepository
                .findByTeacherIdAndYearAndMonth(teacher.getId(), year, month);
        
        if (existingCalculation.isPresent()) {
            return convertToDto(existingCalculation.get());
        }

        // Get teacher's student payments for the month
        BigDecimal totalStudentPayments = paymentRepository
                .sumTeacherStudentPayments(teacher.getId(), year, month);
        
        int totalStudents = paymentRepository
                .countTeacherPaidStudents(teacher.getId(), year, month);

        // Calculate salary based on salary type
        BigDecimal baseSalary = teacher.getBaseSalary() != null ? teacher.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal paymentBasedSalary = BigDecimal.ZERO;
        BigDecimal totalSalary;

        switch (teacher.getSalaryType()) {
            case FIXED:
                totalSalary = baseSalary;
                break;
            case PERCENTAGE:
                paymentBasedSalary = totalStudentPayments
                        .multiply(teacher.getPaymentPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalSalary = paymentBasedSalary;
                break;
            case MIXED:
                paymentBasedSalary = totalStudentPayments
                        .multiply(teacher.getPaymentPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalSalary = baseSalary.add(paymentBasedSalary);
                break;
            default:
                totalSalary = baseSalary;
        }

        // Save calculation
        TeacherSalaryCalculation calculation = new TeacherSalaryCalculation();
        calculation.setTeacher(teacher);
        calculation.setYear(year);
        calculation.setMonth(month);
        calculation.setBaseSalary(baseSalary);
        calculation.setPaymentBasedSalary(paymentBasedSalary);
        calculation.setTotalSalary(totalSalary);
        calculation.setTotalStudentPayments(totalStudentPayments);
        calculation.setTotalStudents(totalStudents);
        calculation.setBranch(teacher.getBranch());

        TeacherSalaryCalculation savedCalculation = salaryCalculationRepository.save(calculation);
        return convertToDto(savedCalculation);
    }

    public TeacherSalaryCalculationDto markSalaryAsPaid(Long calculationId) {
        TeacherSalaryCalculation calculation = salaryCalculationRepository.findById(calculationId)
                .orElseThrow(() -> new RuntimeException("Salary calculation not found with id: " + calculationId));
        
        calculation.setStatus(SalaryCalculationStatus.PAID);
        TeacherSalaryCalculation savedCalculation = salaryCalculationRepository.save(calculation);
        return convertToDto(savedCalculation);
    }

    private TeacherSalaryCalculationDto convertToDto(TeacherSalaryCalculation calculation) {
        TeacherSalaryCalculationDto dto = new TeacherSalaryCalculationDto();
        dto.setId(calculation.getId());
        dto.setTeacherId(calculation.getTeacher().getId());
        dto.setTeacherName(calculation.getTeacher().getFirstName() + " " + calculation.getTeacher().getLastName());
        dto.setYear(calculation.getYear());
        dto.setMonth(calculation.getMonth());
        dto.setBaseSalary(calculation.getBaseSalary());
        dto.setPaymentBasedSalary(calculation.getPaymentBasedSalary());
        dto.setTotalSalary(calculation.getTotalSalary());
        dto.setTotalStudentPayments(calculation.getTotalStudentPayments());
        dto.setTotalStudents(calculation.getTotalStudents());
        dto.setStatus(calculation.getStatus().name());
        dto.setBranchId(calculation.getBranch().getId());
        dto.setBranchName(calculation.getBranch().getName());
        dto.setCreatedAt(calculation.getCreatedAt());
        return dto;
    }
}