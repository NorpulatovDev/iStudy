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

    // Calculate salary on-demand (not stored) with group information
    @Transactional(readOnly = true)
    public SalaryCalculationDto calculateTeacherSalary(Long teacherId, int year, int month) {
        Teacher teacher = teacherRepository.findByIdWithBranch(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        // Get teacher's groups with relations
        List<Group> teacherGroups = groupRepository.findByTeacherIdWithRelations(teacherId);

        // Calculate group salary information
        List<GroupSalaryInfo> groupInfos = new ArrayList<>();
        BigDecimal totalStudentPayments = BigDecimal.ZERO;
        int totalStudents = 0;

        for (Group group : teacherGroups) {
            // Count students in this group who paid in the specified month
            int groupStudentCount = 0;
            BigDecimal groupPayments = BigDecimal.ZERO;

            if (group.getStudents() != null) {
                for (Student student : group.getStudents()) {
                    // Check if student made payment for this group in the specified month
                    BigDecimal studentGroupPayment = paymentRepository.getTotalPaidByStudentInGroupForMonth(
                            student.getId(), group.getId(), year, month);

                    if (studentGroupPayment.compareTo(BigDecimal.ZERO) > 0) {
                        groupStudentCount++;
                        groupPayments = groupPayments.add(studentGroupPayment);
                    }
                }
            }

            totalStudents += groupStudentCount;
            totalStudentPayments = totalStudentPayments.add(groupPayments);

            // Create group info
            GroupSalaryInfo groupInfo = new GroupSalaryInfo(
                    group.getId(),
                    group.getName(),
                    group.getCourse() != null ? group.getCourse().getName() : "N/A",
                    groupStudentCount,
                    groupPayments
            );
            groupInfos.add(groupInfo);
        }

        // Calculate salary based on teacher's salary type
        BigDecimal baseSalary = teacher.getBaseSalary() != null ? teacher.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal paymentBasedSalary = BigDecimal.ZERO;
        BigDecimal totalSalary;

        switch (teacher.getSalaryType()) {
            case FIXED:
                totalSalary = baseSalary;
                break;
            case PERCENTAGE:
                paymentBasedSalary = totalStudentPayments
                        .multiply(teacher.getPaymentPercentage() != null ? teacher.getPaymentPercentage() : BigDecimal.ZERO)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalSalary = paymentBasedSalary;
                break;
            case MIXED:
                paymentBasedSalary = totalStudentPayments
                        .multiply(teacher.getPaymentPercentage() != null ? teacher.getPaymentPercentage() : BigDecimal.ZERO)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalSalary = baseSalary.add(paymentBasedSalary);
                break;
            default:
                totalSalary = baseSalary;
        }

        // Get already paid amount
        BigDecimal alreadyPaid = salaryPaymentRepository.sumByTeacherAndYearAndMonth(teacherId, year, month);
        alreadyPaid = alreadyPaid != null ? alreadyPaid : BigDecimal.ZERO;

        // Calculate remaining amount
        BigDecimal remainingAmount = totalSalary.subtract(alreadyPaid);
        remainingAmount = remainingAmount.compareTo(BigDecimal.ZERO) > 0 ? remainingAmount : BigDecimal.ZERO;

        // Create DTO with constructor that matches the existing constructor
        SalaryCalculationDto dto = new SalaryCalculationDto();
        dto.setTeacherId(teacherId);
        dto.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
        dto.setYear(year);
        dto.setMonth(month);
        dto.setBaseSalary(baseSalary);
        dto.setPaymentBasedSalary(paymentBasedSalary);
        dto.setTotalSalary(totalSalary);
        dto.setTotalStudentPayments(totalStudentPayments);
        dto.setTotalStudents(totalStudents);
        dto.setAlreadyPaid(alreadyPaid);
        dto.setRemainingAmount(remainingAmount);
        dto.setBranchId(teacher.getBranch().getId());
        dto.setBranchName(teacher.getBranch().getName());
//        dto.setGroups(groupInfos);
        // Set group information separately
        dto.setGroups(groupInfos);

        return dto;
    }

    // Calculate salaries for all teachers in branch
    @Transactional(readOnly = true)
    public List<SalaryCalculationDto> calculateSalariesForBranch(Long branchId, int year, int month) {
        List<Teacher> teachers = teacherRepository.findByBranchIdWithBranch(branchId);

        return teachers.stream()
                .map(teacher -> calculateTeacherSalary(teacher.getId(), year, month))
                .collect(Collectors.toList());
    }

    // Create salary payment with validation
    @Transactional
    public TeacherSalaryPaymentDto createSalaryPayment(CreateSalaryPaymentRequest request) {
        // Get teacher
        Teacher teacher = teacherRepository.findByIdWithBranch(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));

        // Get branch
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        // Calculate current expected salary
        SalaryCalculationDto calculation = calculateTeacherSalary(
                request.getTeacherId(), request.getYear(), request.getMonth());

        // Validate payment amount
        if (request.getAmount().compareTo(calculation.getRemainingAmount()) > 0) {
            throw new RuntimeException("To'lov miqdori qolgan maoshdan oshib ketmoqda! Qolgan miqdor: " +
                    calculation.getRemainingAmount());
        }

        // Create payment record
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

    // Get salary payments by branch
    @Transactional(readOnly = true)
    public List<TeacherSalaryPaymentDto> getSalaryPaymentsByBranch(Long branchId) {
        return salaryPaymentRepository.findByBranchIdWithDetails(branchId).stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    // Get salary payments by teacher
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

    // Get salary history for teacher
    @Transactional(readOnly = true)
    public List<TeacherSalaryHistoryDto> getTeacherSalaryHistory(Long teacherId) {
        Teacher teacher = teacherRepository.findByIdWithBranch(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        List<Object[]> yearMonthPairs = salaryPaymentRepository.findDistinctYearMonthByTeacherId(teacherId);
        List<TeacherSalaryHistoryDto> history = new ArrayList<>();

        for (Object[] pair : yearMonthPairs) {
            int year = (Integer) pair[0];
            int month = (Integer) pair[1];

            // Calculate salary for this month
            SalaryCalculationDto calculation = calculateTeacherSalary(teacherId, year, month);

            // Get payment details
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
                    remainingAmount.compareTo(BigDecimal.ZERO) == 0,
                    lastPaymentDate,
                    paymentCount
            );

            history.add(historyItem);
        }

        return history;
    }

    // Get remaining amount for teacher and month
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

    // Convert payment entity to DTO
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