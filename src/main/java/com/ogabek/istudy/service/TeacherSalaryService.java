package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateSalaryPaymentRequest;
import com.ogabek.istudy.dto.response.GroupSalaryInfo;
import com.ogabek.istudy.dto.response.SalaryCalculationDto;
import com.ogabek.istudy.dto.response.TeacherSalaryHistoryDto;
import com.ogabek.istudy.dto.response.TeacherSalaryPaymentDto;
import com.ogabek.istudy.entity.*;
import com.ogabek.istudy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherSalaryService {
    private final TeacherSalaryPaymentRepository salaryPaymentRepository;
    private final TeacherRepository teacherRepository;
    private final BranchRepository branchRepository;
    private final PaymentRepository paymentRepository;
    private final GroupRepository groupRepository;

    // Calculate salary on-demand (not stored) with detailed group information
    @Transactional(readOnly = true)
    public SalaryCalculationDto calculateTeacherSalary(Long teacherId, int year, int month) {
        Teacher teacher = teacherRepository.findByIdWithBranch(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        // Get teacher's groups with relations
        List<Group> teacherGroups = groupRepository.findByTeacherIdWithRelations(teacherId);

        // Calculate group salary information with enhanced details
        List<GroupSalaryInfo> groupInfos = new ArrayList<>();
        BigDecimal totalStudentPayments = BigDecimal.ZERO;
        int totalPaidStudents = 0;

        for (Group group : teacherGroups) {
            // Count total students enrolled in this group
            int totalStudentsInGroup = group.getStudents() != null ? group.getStudents().size() : 0;

            // Count students in this group who paid in the specified month
            int paidStudentCount = 0;
            BigDecimal groupPayments = BigDecimal.ZERO;

            if (group.getStudents() != null) {
                for (Student student : group.getStudents()) {
                    // Check if student made payment for this group in the specified month
                    BigDecimal studentGroupPayment = paymentRepository.getTotalPaidByStudentInGroupForMonth(
                            student.getId(), group.getId(), year, month);

                    if (studentGroupPayment.compareTo(BigDecimal.ZERO) > 0) {
                        paidStudentCount++;
                        groupPayments = groupPayments.add(studentGroupPayment);
                    }
                }
            }

            totalPaidStudents += paidStudentCount;
            totalStudentPayments = totalStudentPayments.add(groupPayments);

            // Get course price for reference
            BigDecimal coursePrice = group.getCourse() != null ? group.getCourse().getPrice() : BigDecimal.ZERO;

            // Create enhanced group info with all details
            GroupSalaryInfo groupInfo = new GroupSalaryInfo(
                    group.getId(),
                    group.getName(),
                    group.getCourse() != null ? group.getCourse().getName() : "N/A",
                    paidStudentCount,           // students who paid
                    groupPayments,              // total payments from this group
                    totalStudentsInGroup,       // total enrolled students
                    coursePrice                 // course price for reference
            );
            groupInfos.add(groupInfo);
        }

        // Calculate salary based on teacher's salary type
        BigDecimal baseSalary = teacher.getBaseSalary() != null ? teacher.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal paymentBasedSalary = BigDecimal.ZERO;
        BigDecimal totalSalary;

        // Calculate salary according to teacher's salary type
        switch (teacher.getSalaryType()) {
            case FIXED:
                // Fixed salary regardless of student payments
                totalSalary = baseSalary;
                paymentBasedSalary = BigDecimal.ZERO;
                break;

            case PERCENTAGE:
                // Salary is percentage of total student payments
                if (teacher.getPaymentPercentage() != null) {
                    paymentBasedSalary = totalStudentPayments
                            .multiply(teacher.getPaymentPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                totalSalary = paymentBasedSalary;
                break;

            case MIXED:
                // Combination of base salary + percentage of payments
                if (teacher.getPaymentPercentage() != null) {
                    paymentBasedSalary = totalStudentPayments
                            .multiply(teacher.getPaymentPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                totalSalary = baseSalary.add(paymentBasedSalary);
                break;

            default:
                // Default to fixed salary if type is not recognized
                totalSalary = baseSalary;
                paymentBasedSalary = BigDecimal.ZERO;
        }

        // Get already paid amount for this teacher in this month
        BigDecimal alreadyPaid = salaryPaymentRepository.sumByTeacherAndYearAndMonth(teacherId, year, month);
        alreadyPaid = alreadyPaid != null ? alreadyPaid : BigDecimal.ZERO;

        // Calculate remaining amount to be paid
        BigDecimal remainingAmount = totalSalary.subtract(alreadyPaid);
        remainingAmount = remainingAmount.compareTo(BigDecimal.ZERO) > 0 ? remainingAmount : BigDecimal.ZERO;

        // Create comprehensive salary calculation DTO
        SalaryCalculationDto dto = new SalaryCalculationDto();
        dto.setTeacherId(teacherId);
        dto.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
        dto.setYear(year);
        dto.setMonth(month);
        dto.setBaseSalary(baseSalary);
        dto.setPaymentBasedSalary(paymentBasedSalary);
        dto.setTotalSalary(totalSalary);
        dto.setTotalStudentPayments(totalStudentPayments);
        dto.setTotalStudents(totalPaidStudents);
        dto.setAlreadyPaid(alreadyPaid);
        dto.setRemainingAmount(remainingAmount);
        dto.setBranchId(teacher.getBranch().getId());
        dto.setBranchName(teacher.getBranch().getName());

        // Set detailed group information
        dto.setGroups(groupInfos);

        return dto;
    }

    // Calculate salaries for all teachers in a branch
    @Transactional(readOnly = true)
    public List<SalaryCalculationDto> calculateSalariesForBranch(Long branchId, int year, int month) {
        List<Teacher> teachers = teacherRepository.findByBranchIdWithBranch(branchId);

        return teachers.stream()
                .map(teacher -> calculateTeacherSalary(teacher.getId(), year, month))
                .collect(Collectors.toList());
    }

    // Create salary payment with comprehensive validation
    @Transactional
    public TeacherSalaryPaymentDto createSalaryPayment(CreateSalaryPaymentRequest request) {
        // Validate teacher exists
        Teacher teacher = teacherRepository.findByIdWithBranch(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));

        // Validate branch exists
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        // Validate payment amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("To'lov miqdori 0 dan katta bo'lishi kerak!");
        }

        // Calculate current expected salary (optional validation)
        SalaryCalculationDto calculation = calculateTeacherSalary(
                request.getTeacherId(), request.getYear(), request.getMonth());

        // Optional: Warn if payment exceeds remaining amount (but allow it)
        if (request.getAmount().compareTo(calculation.getRemainingAmount()) > 0) {
            // Log warning but don't prevent payment - allows advance payments or corrections
            // Could add a warning flag to the response if needed
        }

        // Create salary payment record
        TeacherSalaryPayment payment = new TeacherSalaryPayment();
        payment.setTeacher(teacher);
        payment.setYear(request.getYear());
        payment.setMonth(request.getMonth());
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setBranch(branch);

        TeacherSalaryPayment savedPayment = salaryPaymentRepository.save(payment);
        return convertPaymentToDto(savedPayment);
    }

    // Get all salary payments for a branch
    @Transactional(readOnly = true)
    public List<TeacherSalaryPaymentDto> getSalaryPaymentsByBranch(Long branchId) {
        return salaryPaymentRepository.findByBranchIdWithDetails(branchId).stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    // Get salary payments for specific teacher
    @Transactional(readOnly = true)
    public List<TeacherSalaryPaymentDto> getSalaryPaymentsByTeacher(Long teacherId) {
        return salaryPaymentRepository.findByTeacherIdWithDetails(teacherId).stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    // Get salary payments for specific teacher and month
    @Transactional(readOnly = true)
    public List<TeacherSalaryPaymentDto> getPaymentsForTeacherAndMonth(Long teacherId, int year, int month) {
        return salaryPaymentRepository.findByTeacherAndYearAndMonthWithDetails(teacherId, year, month).stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    // Get comprehensive salary history for teacher
    @Transactional(readOnly = true)
    public List<TeacherSalaryHistoryDto> getTeacherSalaryHistory(Long teacherId) {
        Teacher teacher = teacherRepository.findByIdWithBranch(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        // Get all months where this teacher has payment records
        List<Object[]> yearMonthPairs = salaryPaymentRepository.findDistinctYearMonthByTeacherId(teacherId);
        List<TeacherSalaryHistoryDto> history = new ArrayList<>();

        for (Object[] pair : yearMonthPairs) {
            int year = (Integer) pair[0];
            int month = (Integer) pair[1];

            // Calculate expected salary for this month
            SalaryCalculationDto calculation = calculateTeacherSalary(teacherId, year, month);

            // Get actual payment details
            BigDecimal totalPaid = salaryPaymentRepository.sumByTeacherAndYearAndMonth(teacherId, year, month);
            LocalDateTime lastPaymentDate = salaryPaymentRepository.getLastPaymentDate(teacherId, year, month);
            int paymentCount = salaryPaymentRepository.countPaymentsByTeacherAndYearAndMonth(teacherId, year, month);

            totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
            BigDecimal remainingAmount = calculation.getTotalSalary().subtract(totalPaid);
            remainingAmount = remainingAmount.compareTo(BigDecimal.ZERO) > 0 ? remainingAmount : BigDecimal.ZERO;

            TeacherSalaryHistoryDto historyItem = new TeacherSalaryHistoryDto(
                    teacherId,
                    teacher.getFirstName() + " " + teacher.getLastName(),
                    year,
                    month,
                    calculation.getTotalSalary(),
                    totalPaid,
                    remainingAmount,
                    remainingAmount.compareTo(BigDecimal.ZERO) == 0, // isFullyPaid
                    lastPaymentDate,
                    paymentCount
            );

            history.add(historyItem);
        }

        // Sort by year and month (newest first)
        history.sort((a, b) -> {
            int yearCompare = Integer.compare(b.getYear(), a.getYear());
            if (yearCompare != 0) return yearCompare;
            return Integer.compare(b.getMonth(), a.getMonth());
        });

        return history;
    }

    // Get remaining salary amount for teacher in specific month
    @Transactional(readOnly = true)
    public BigDecimal getRemainingAmountForTeacher(Long teacherId, int year, int month) {
        SalaryCalculationDto calculation = calculateTeacherSalary(teacherId, year, month);
        return calculation.getRemainingAmount();
    }

    // Delete salary payment
    @Transactional
    public void deleteSalaryPayment(Long paymentId) {
        if (!salaryPaymentRepository.existsById(paymentId)) {
            throw new RuntimeException("Salary payment not found with id: " + paymentId);
        }
        salaryPaymentRepository.deleteById(paymentId);
    }

    // Convert salary payment entity to DTO
    private TeacherSalaryPaymentDto convertPaymentToDto(TeacherSalaryPayment payment) {
        TeacherSalaryPaymentDto dto = new TeacherSalaryPaymentDto();
        dto.setId(payment.getId());
        dto.setTeacherId(payment.getTeacher().getId());
        dto.setTeacherName(payment.getTeacher().getFirstName() + " " + payment.getTeacher().getLastName());
        dto.setYear(payment.getYear());
        dto.setMonth(payment.getMonth());
        dto.setAmount(payment.getAmount());
        dto.setDescription(payment.getDescription());
        dto.setBranchId(payment.getBranch().getId());
        dto.setBranchName(payment.getBranch().getName());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}