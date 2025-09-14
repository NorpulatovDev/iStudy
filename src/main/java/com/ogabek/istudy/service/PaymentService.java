package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreatePaymentRequest;
import com.ogabek.istudy.dto.response.PaymentDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Course;
import com.ogabek.istudy.entity.Payment;
import com.ogabek.istudy.entity.Student;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.CourseRepository;
import com.ogabek.istudy.repository.PaymentRepository;
import com.ogabek.istudy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Course course = courseRepository.findByIdWithBranch(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        LocalDate now = LocalDate.now();

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setCourse(course);
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setBranch(branch);
        payment.setPaymentYear(now.getYear());
        payment.setPaymentMonth(now.getMonthValue());

        Payment savedPayment = paymentRepository.save(payment);

        // Fetch the saved payment with all relations for proper DTO conversion
        Payment paymentWithRelations = paymentRepository.findByIdWithAllRelations(savedPayment.getId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch created payment"));

        return convertToDto(paymentWithRelations);
    }

    // NEW: Get payments by date range
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

    // NEW: Get payments by month/year
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByMonth(Long branchId, int year, int month) {
        return paymentRepository.findByBranchIdAndPaymentYearAndPaymentMonthWithRelations(branchId, year, month)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // NEW: Search payments by student name
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

    // NEW: Get recent payments
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

        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}