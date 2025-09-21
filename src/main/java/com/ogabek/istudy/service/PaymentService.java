package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreatePaymentRequest;
import com.ogabek.istudy.dto.response.PaymentDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Course;
import com.ogabek.istudy.entity.Group;
import com.ogabek.istudy.entity.Payment;
import com.ogabek.istudy.entity.Student;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.CourseRepository;
import com.ogabek.istudy.repository.GroupRepository;
import com.ogabek.istudy.repository.PaymentRepository;
import com.ogabek.istudy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByBranch(Long branchId) {
        return paymentRepository.findByBranchIdWithAllRelations(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByStudent(Long studentId) {
        return paymentRepository.findByStudentIdWithRelations(studentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return convertToDto(payment);
    }

    @Transactional
    public PaymentDto createPayment(CreatePaymentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + request.getStudentId()));

        // Get group and validate student is in this group
        Group group = groupRepository.findByIdWithAllRelations(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + request.getGroupId()));

        // Validate student is member of this group
        if (group.getStudents() == null || !group.getStudents().contains(student)) {
            throw new RuntimeException("O'quvchi bu guruhda yo'q!");
        }

        // Get course from group
        if (group.getCourse() == null) {
            throw new RuntimeException("Guruhda kurs mavjud emas!");
        }
        Course course = group.getCourse();

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        // Use payment year and month from request
        int paymentYear = request.getPaymentYear();
        int paymentMonth = request.getPaymentMonth();

        // Validation: Only check that payment amount is positive
        // Allow payments exceeding course price (for advance payments, penalties, etc.)
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("To'lov miqdori 0 dan katta bo'lishi kerak!");
        }

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setCourse(course);
        payment.setGroup(group);
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setBranch(branch);
        payment.setPaymentYear(paymentYear);
        payment.setPaymentMonth(paymentMonth);

        Payment savedPayment = paymentRepository.save(payment);

        // Fetch the saved payment with all relations for proper DTO conversion
        Payment paymentWithRelations = paymentRepository.findByIdWithAllRelations(savedPayment.getId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch created payment"));

        return convertToDto(paymentWithRelations);
    }

    // NEW: Update payment amount - allows overpayment
    @Transactional
    public PaymentDto updatePaymentAmount(Long id, BigDecimal newAmount) {
        // Get existing payment
        Payment payment = paymentRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        // Validate new amount is positive
        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("To'lov miqdori 0 dan katta bo'lishi kerak!");
        }

        // Update payment amount - no maximum limit check
        payment.setAmount(newAmount);
        Payment savedPayment = paymentRepository.save(payment);

        // Return updated payment with all relations
        Payment updatedPaymentWithRelations = paymentRepository.findByIdWithAllRelations(savedPayment.getId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch updated payment"));

        return convertToDto(updatedPaymentWithRelations);
    }

    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }

    // Get payments by date range
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByDateRange(Long branchId, LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByBranchIdAndCreatedAtBetweenWithRelations(
                        branchId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get payments by month/year
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByMonth(Long branchId, int year, int month) {
        return paymentRepository.findByBranchIdAndPaymentYearAndPaymentMonthWithRelations(branchId, year, month)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Search payments by student name
    @Transactional(readOnly = true)
    public List<PaymentDto> searchPaymentsByStudentName(Long branchId, String studentName) {
        return paymentRepository.findByBranchIdWithAllRelations(branchId).stream()
                .filter(payment -> {
                    if (payment.getStudent() != null) {
                        String fullName = payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName();
                        return fullName.toLowerCase().contains(studentName.toLowerCase());
                    }
                    return false;
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get recent payments
    @Transactional(readOnly = true)
    public List<PaymentDto> getRecentPayments(Long branchId, int limit) {
        return paymentRepository.findByBranchIdWithAllRelations(branchId).stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());

        // Safe access to student properties
        if (payment.getStudent() != null) {
            dto.setStudentId(payment.getStudent().getId());
            dto.setStudentName(payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName());
        }

        // Safe access to course properties
        if (payment.getCourse() != null) {
            dto.setCourseId(payment.getCourse().getId());
            dto.setCourseName(payment.getCourse().getName());
        }

        dto.setAmount(payment.getAmount());
        dto.setDescription(payment.getDescription());
        dto.setStatus(payment.getStatus().name());

        // Safe access to branch properties
        if (payment.getBranch() != null) {
            dto.setBranchId(payment.getBranch().getId());
            dto.setBranchName(payment.getBranch().getName());
        }

        // Safe access to group properties
        if (payment.getGroup() != null) {
            dto.setGroupId(payment.getGroup().getId());
            dto.setGroupName(payment.getGroup().getName());
        }

        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}