package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateGroupRequest;
import com.ogabek.istudy.dto.response.GroupDto;
import com.ogabek.istudy.dto.response.StudentDto;
import com.ogabek.istudy.entity.*;
import com.ogabek.istudy.repository.*;
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
public class GroupService {
    private final GroupRepository groupRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final BranchRepository branchRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<GroupDto> getGroupsByBranch(Long branchId) {
        return groupRepository.findByBranchIdWithAllRelations(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getUnpaidStudentsByGroup(Long groupId, Integer year, Integer month) {
        Group group = groupRepository.findByIdWithAllRelations(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        return studentRepository.findUnpaidStudentsByBranchAndMonth(group.getBranch().getId(), targetYear, targetMonth)
                .stream()
                .filter(student -> group.getStudents() != null && group.getStudents().contains(student))
                .map(student -> convertStudentToDto(student, targetYear, targetMonth))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupDto getGroupById(Long id) {
        Group group = groupRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
        return convertToDto(group);
    }

    @Transactional
    public GroupDto createGroup(CreateGroupRequest request) {
        Course course = courseRepository.findByIdWithBranch(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));

        Teacher teacher = teacherRepository.findByIdWithBranch(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        Group group = new Group();
        group.setName(request.getName());
        group.setCourse(course);
        group.setTeacher(teacher);
        group.setBranch(branch);

        // SIMPLE: Just assign strings directly
        group.setStartTime(request.getStartTime());
        group.setEndTime(request.getEndTime());

        if (request.getDaysOfWeek() != null && !request.getDaysOfWeek().isEmpty()) {
            group.setDaysOfWeek(String.join(",", request.getDaysOfWeek()));
        }

        // Add students to group
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            Set<Student> students = new HashSet<>();
            for (Long studentId : request.getStudentIds()) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
                students.add(student);
            }
            group.setStudents(students);
        }

        Group savedGroup = groupRepository.save(group);

        // Fetch the saved group with all relations for proper DTO conversion
        Group groupWithRelations = groupRepository.findByIdWithAllRelations(savedGroup.getId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch created group"));

        return convertToDto(groupWithRelations);
    }

    // Update your updateGroup method similarly:
    @Transactional
    public GroupDto updateGroup(Long id, CreateGroupRequest request) {
        Group group = groupRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        Course course = courseRepository.findByIdWithBranch(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));

        Teacher teacher = teacherRepository.findByIdWithBranch(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        group.setName(request.getName());
        group.setCourse(course);
        group.setTeacher(teacher);
        group.setBranch(branch);

        // SIMPLE: Just assign strings directly
        group.setStartTime(request.getStartTime());
        group.setEndTime(request.getEndTime());

        if (request.getDaysOfWeek() != null && !request.getDaysOfWeek().isEmpty()) {
            group.setDaysOfWeek(String.join(",", request.getDaysOfWeek()));
        }

        // Update students in group
        if (request.getStudentIds() != null) {
            Set<Student> students = new HashSet<>();
            for (Long studentId : request.getStudentIds()) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
                students.add(student);
            }
            group.setStudents(students);
        }

        Group savedGroup = groupRepository.save(group);

        // Fetch the saved group with all relations for proper DTO conversion
        Group groupWithRelations = groupRepository.findByIdWithAllRelations(savedGroup.getId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch updated group"));

        return convertToDto(groupWithRelations);
    }

    @Transactional
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new RuntimeException("Guruh topilmadi: " + id);
        }

        Group group = groupRepository.findByIdWithStudents(id);

        // Check if group has students
        if (group.getStudents() != null && !group.getStudents().isEmpty()) {
            throw new RuntimeException("Bu guruhda o'quvchilar borligi uchun uni o'chira olmaysiz. Avval o'quvchilarni boshqa guruhlarga ko'chiring!");
        }

        groupRepository.deleteById(id);
    }

    // Get all students in a group with their payment status
    @Transactional(readOnly = true)
    public List<StudentDto> getGroupStudents(Long groupId, Integer year, Integer month) {
        Group group = groupRepository.findByIdWithAllRelations(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        return group.getStudents().stream()
                .map(student -> convertStudentToDto(student, targetYear, targetMonth))
                .collect(Collectors.toList());
    }

    // Get groups by teacher
    @Transactional(readOnly = true)
    public List<GroupDto> getGroupsByTeacher(Long teacherId) {
        return groupRepository.findByTeacherIdWithRelations(teacherId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get groups by course
    @Transactional(readOnly = true)
    public List<GroupDto> getGroupsByCourse(Long courseId) {
        return groupRepository.findByCourseIdWithRelations(courseId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Add student to group
    @Transactional
    public GroupDto addStudentToGroup(Long groupId, Long studentId) {
        Group group = groupRepository.findByIdWithAllRelations(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        if (group.getStudents() == null) {
            group.setStudents(new HashSet<>());
        }

        group.getStudents().add(student);
        Group savedGroup = groupRepository.save(group);

        // Fetch with relations for DTO conversion
        Group groupWithRelations = groupRepository.findByIdWithAllRelations(savedGroup.getId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch updated group"));

        return convertToDto(groupWithRelations);
    }

    // Remove student from group
    @Transactional
    public GroupDto removeStudentFromGroup(Long groupId, Long studentId) {
        Group group = groupRepository.findByIdWithAllRelations(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        if (group.getStudents() != null) {
            group.getStudents().remove(student);
            Group savedGroup = groupRepository.save(group);

            // Fetch with relations for DTO conversion
            Group groupWithRelations = groupRepository.findByIdWithAllRelations(savedGroup.getId())
                    .orElseThrow(() -> new RuntimeException("Failed to fetch updated group"));

            return convertToDto(groupWithRelations);
        }

        return convertToDto(group);
    }

    private GroupDto convertToDto(Group group) {
        GroupDto dto = new GroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());

        // Safe access to course properties
        if (group.getCourse() != null) {
            dto.setCourseId(group.getCourse().getId());
            dto.setCourseName(group.getCourse().getName());
        }

        // Safe access to teacher properties
        if (group.getTeacher() != null) {
            dto.setTeacherId(group.getTeacher().getId());
            dto.setTeacherName(group.getTeacher().getFirstName() + " " + group.getTeacher().getLastName());
        }

        // Safe access to branch properties
        if (group.getBranch() != null) {
            dto.setBranchId(group.getBranch().getId());
            dto.setBranchName(group.getBranch().getName());
        }

        // UPDATED: Direct string assignment (no conversion needed)
        dto.setStartTime(group.getStartTime());  // String to String
        dto.setEndTime(group.getEndTime());      // String to String

        if (group.getDaysOfWeek() != null && !group.getDaysOfWeek().isEmpty()) {
            dto.setDaysOfWeek(Arrays.asList(group.getDaysOfWeek().split(",")));
        } else {
            dto.setDaysOfWeek(new ArrayList<>());
        }

        // Convert students if present
        if (group.getStudents() != null) {
            LocalDate now = LocalDate.now();
            List<StudentDto> studentDtos = group.getStudents().stream()
                    .map(student -> convertStudentToDto(student, now.getYear(), now.getMonthValue()))
                    .collect(Collectors.toList());
            dto.setStudents(studentDtos);
        }

        dto.setCreatedAt(group.getCreatedAt());
        return dto;
    }

    // Enhanced convertStudentToDto with payment status calculation
    private StudentDto convertStudentToDto(Student student, int year, int month) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setPhoneNumber(student.getPhoneNumber());

        // Safe access to branch properties
        if (student.getBranch() != null) {
            dto.setBranchId(student.getBranch().getId());
            dto.setBranchName(student.getBranch().getName());
        }

        dto.setCreatedAt(student.getCreatedAt());

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
}