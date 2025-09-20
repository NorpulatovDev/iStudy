package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateStudentRequest;
import com.ogabek.istudy.dto.response.GroupDto;
import com.ogabek.istudy.dto.response.PaymentDto;
import com.ogabek.istudy.dto.response.StudentDto;
import com.ogabek.istudy.dto.response.UnpaidStudentDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Group;
import com.ogabek.istudy.entity.Student;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.GroupRepository;
import com.ogabek.istudy.repository.PaymentRepository;
import com.ogabek.istudy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final BranchRepository branchRepository;
    private final PaymentRepository paymentRepository;
    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByBranch(Long branchId) {
        LocalDate now = LocalDate.now();
        return studentRepository.findByBranchIdWithBranch(branchId).stream()
                .map(student -> convertToDto(student, now.getYear(), now.getMonthValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByBranch(Long branchId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        return studentRepository.findByBranchIdWithBranch(branchId).stream()
                .map(student -> convertToDto(student, targetYear, targetMonth))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByGroup(Long groupId, Integer year, Integer month) {
        Group group = groupRepository.findByIdWithAllRelations(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        if (group.getStudents() == null || group.getStudents().isEmpty()) {
            return new ArrayList<>();
        }

        return group.getStudents().stream()
                .map(student -> convertToDto(student, targetYear, targetMonth))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnpaidStudentDto> getUnpaidStudents(Long branchId, Integer year, Integer month) {
        List<UnpaidStudentDto> result = new ArrayList<>();
        List<Group> branchGroups = groupRepository.findByBranchIdWithAllRelations(branchId);

        for (Group group : branchGroups) {
            if (group.getStudents() != null && group.getCourse() != null) {
                for (Student student : group.getStudents()) {
                    BigDecimal totalPaid;

                    if (year == null || month == null) {
                        // All-time unpaid
                        totalPaid = paymentRepository.findByStudentIdWithRelations(student.getId())
                                .stream()
                                .filter(payment -> payment.getCourse().getId().equals(group.getCourse().getId()))
                                .map(payment -> payment.getAmount())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                    } else {
                        // Monthly unpaid
                        totalPaid = paymentRepository.findByStudentIdWithRelations(student.getId())
                                .stream()
                                .filter(payment -> payment.getPaymentYear() == year &&
                                        payment.getPaymentMonth() == month &&
                                        payment.getCourse().getId().equals(group.getCourse().getId()))
                                .map(payment -> payment.getAmount())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                    }

                    BigDecimal remainingAmount = group.getCourse().getPrice().subtract(totalPaid);

                    if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                        result.add(new UnpaidStudentDto(
                                student.getId(),
                                student.getFirstName(),
                                student.getLastName(),
                                student.getPhoneNumber(),
                                student.getParentPhoneNumber(),
                                remainingAmount,
                                group.getId(),
                                group.getName()
                        ));
                    }
                }
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<StudentDto> searchStudentsByName(Long branchId, String name) {
        LocalDate now = LocalDate.now();
        return studentRepository.findByBranchIdAndFullName(branchId, name).stream()
                .map(student -> convertToDto(student, now.getYear(), now.getMonthValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getStudentPaymentHistory(Long studentId) {
        return paymentRepository.findByStudentId(studentId).stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getStudentGroups(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        return groupRepository.findByBranchIdWithAllRelations(student.getBranch().getId()).stream()
                .filter(group -> group.getStudents() != null && group.getStudents().contains(student))
                .map(this::convertGroupToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentStatistics(Long branchId) {
        List<Student> allStudents = studentRepository.findByBranchId(branchId);
        LocalDate now = LocalDate.now();
        List<Student> unpaidStudents = studentRepository.findUnpaidStudentsByBranchAndMonth(
                branchId, now.getYear(), now.getMonthValue());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalStudents", allStudents.size());
        statistics.put("paidStudents", allStudents.size() - unpaidStudents.size());
        statistics.put("unpaidStudents", unpaidStudents.size());
        statistics.put("paymentRate", allStudents.size() > 0 ?
                (double)(allStudents.size() - unpaidStudents.size()) / allStudents.size() * 100 : 0);

        return statistics;
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getRecentStudents(Long branchId, int limit) {
        LocalDate now = LocalDate.now();
        return studentRepository.findByBranchId(branchId).stream()
                .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
                .limit(limit)
                .map(student -> convertToDto(student, now.getYear(), now.getMonthValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        LocalDate now = LocalDate.now();
        return convertToDto(student, now.getYear(), now.getMonthValue());
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id, Integer year, Integer month) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        return convertToDto(student, targetYear, targetMonth);
    }

    @Transactional
    public StudentDto createStudent(CreateStudentRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        // Create student
        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setParentPhoneNumber(request.getParentPhoneNumber()); // new
        student.setBranch(branch);

        Student savedStudent = studentRepository.save(student);

        // Add student to multiple groups if provided
        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            for (Long groupId : request.getGroupIds()) {
                Group group = groupRepository.findByIdWithAllRelations(groupId)
                        .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

                // Verify group belongs to the same branch
                if (!group.getBranch().getId().equals(request.getBranchId())) {
                    throw new RuntimeException("Group " + groupId + " does not belong to branch " + request.getBranchId());
                }

                if (group.getStudents() == null) {
                    group.setStudents(new HashSet<>());
                }
                group.getStudents().add(savedStudent);
                groupRepository.save(group);
            }
        }

        LocalDate now = LocalDate.now();
        return convertToDto(savedStudent, now.getYear(), now.getMonthValue());
    }

    @Transactional
    public StudentDto updateStudent(Long id, CreateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        // Update basic student info
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setParentPhoneNumber(request.getParentPhoneNumber()); // new
        student.setBranch(branch);

        Student savedStudent = studentRepository.save(student);

        // Update group memberships
        // First, remove student from all current groups
        List<Group> currentGroups = groupRepository.findByBranchIdWithAllRelations(branch.getId()).stream()
                .filter(group -> group.getStudents() != null && group.getStudents().contains(student))
                .collect(Collectors.toList());

        for (Group group : currentGroups) {
            group.getStudents().remove(student);
            groupRepository.save(group);
        }

        // Then, add student to new groups
        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            for (Long groupId : request.getGroupIds()) {
                Group group = groupRepository.findByIdWithAllRelations(groupId)
                        .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

                // Verify group belongs to the same branch
                if (!group.getBranch().getId().equals(request.getBranchId())) {
                    throw new RuntimeException("Group " + groupId + " does not belong to branch " + request.getBranchId());
                }

                if (group.getStudents() == null) {
                    group.setStudents(new HashSet<>());
                }
                group.getStudents().add(savedStudent);
                groupRepository.save(group);
            }
        }

        LocalDate now = LocalDate.now();
        return convertToDto(savedStudent, now.getYear(), now.getMonthValue());
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // Remove student from all groups before deleting
        List<Group> studentGroups = groupRepository.findByBranchIdWithAllRelations(student.getBranch().getId()).stream()
                .filter(group -> group.getStudents() != null && group.getStudents().contains(student))
                .collect(Collectors.toList());

        for (Group group : studentGroups) {
            group.getStudents().remove(student);
            groupRepository.save(group);
        }

        studentRepository.deleteById(id);
    }

    // UPDATED: Enhanced convertToDto method with group information
    private StudentDto convertToDto(Student student, int year, int month) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setParentPhoneNumber(student.getParentPhoneNumber()); // new

        // Safe access to branch properties
        if (student.getBranch() != null) {
            dto.setBranchId(student.getBranch().getId());
            dto.setBranchName(student.getBranch().getName());
        }

        dto.setCreatedAt(student.getCreatedAt());

        // Get groups the student belongs to (with eager loading to avoid lazy loading issues)
        if (student.getBranch() != null) {
            List<Group> studentGroups = groupRepository.findByBranchIdWithAllRelations(student.getBranch().getId()).stream()
                    .filter(group -> group.getStudents() != null && group.getStudents().contains(student))
                    .collect(Collectors.toList());

            List<StudentDto.GroupInfo> groupInfos = studentGroups.stream()
                    .map(group -> {
                        String teacherName = group.getTeacher() != null ?
                                group.getTeacher().getFirstName() + " " + group.getTeacher().getLastName() : null;
                        return new StudentDto.GroupInfo(
                                group.getId(),
                                group.getName(),
                                group.getCourse() != null ? group.getCourse().getId() : null,
                                group.getCourse() != null ? group.getCourse().getName() : null,
                                teacherName
                        );
                    })
                    .collect(Collectors.toList());

            dto.setGroups(groupInfos);
        }

        // Calculate payment status for the specified month/year
        calculatePaymentStatus(dto, student.getId(), year, month);

        return dto;
    }

    // Calculate payment status for a student
    private void calculatePaymentStatus(StudentDto dto, Long studentId, int year, int month) {
        // Check if student has paid in the specified month
        Boolean hasPaid = studentRepository.hasStudentPaidInMonth(studentId, year, month);
        dto.setHasPaidInMonth(hasPaid != null ? hasPaid : false);

        // Get total amount paid in the month
        BigDecimal totalPaid = studentRepository.getTotalPaidByStudentInMonth(studentId, year, month);
        dto.setTotalPaidInMonth(totalPaid != null ? totalPaid : BigDecimal.ZERO);

        // Get expected monthly payment amount
        BigDecimal expectedPayment = studentRepository.getExpectedMonthlyPaymentForStudent(studentId);
        expectedPayment = expectedPayment != null ? expectedPayment : BigDecimal.ZERO;

        // Calculate remaining amount
        BigDecimal remaining = expectedPayment.subtract(dto.getTotalPaidInMonth());
        dto.setRemainingAmount(remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO);

        // Determine payment status
        if (dto.getTotalPaidInMonth().compareTo(BigDecimal.ZERO) == 0) {
            dto.setPaymentStatus("UNPAID");
        } else if (dto.getTotalPaidInMonth().compareTo(expectedPayment) >= 0) {
            dto.setPaymentStatus("PAID");
        } else {
            dto.setPaymentStatus("PARTIAL");
        }

        // Get last payment date
        LocalDateTime lastPaymentDate = studentRepository.getLastPaymentDate(studentId);
        dto.setLastPaymentDate(lastPaymentDate);
    }

    // Keep existing helper methods
    private PaymentDto convertPaymentToDto(com.ogabek.istudy.entity.Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setStudentId(payment.getStudent().getId());
        dto.setStudentName(payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName());
        dto.setCourseId(payment.getCourse().getId());
        dto.setCourseName(payment.getCourse().getName());
        dto.setGroupId(payment.getGroup().getId()); // NEW
        dto.setGroupName(payment.getGroup().getName()); // NEW
        dto.setAmount(payment.getAmount());
        dto.setDescription(payment.getDescription());
        dto.setStatus(payment.getStatus().name());
        dto.setBranchId(payment.getBranch().getId());
        dto.setBranchName(payment.getBranch().getName());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

    // In StudentService.java, update the convertGroupToDto method:

    private GroupDto convertGroupToDto(Group group) {
        GroupDto dto = new GroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCourseId(group.getCourse().getId());
        dto.setCourseName(group.getCourse().getName());

        if (group.getTeacher() != null) {
            dto.setTeacherId(group.getTeacher().getId());
            dto.setTeacherName(group.getTeacher().getFirstName() + " " + group.getTeacher().getLastName());
        }

        dto.setBranchId(group.getBranch().getId());
        dto.setBranchName(group.getBranch().getName());

        // UPDATED: Set schedule fields as String (not LocalTime)
        dto.setStartTime(group.getStartTime());  // Already String now
        dto.setEndTime(group.getEndTime());      // Already String now

        if (group.getDaysOfWeek() != null && !group.getDaysOfWeek().isEmpty()) {
            dto.setDaysOfWeek(Arrays.asList(group.getDaysOfWeek().split(",")));
        } else {
            dto.setDaysOfWeek(new ArrayList<>());
        }

        dto.setCreatedAt(group.getCreatedAt());
        return dto;
    }
}