package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateGroupRequest;
import com.ogabek.istudy.dto.response.GroupDto;
import com.ogabek.istudy.dto.response.StudentDto;
import com.ogabek.istudy.entity.*;
import com.ogabek.istudy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final BranchRepository branchRepository;
    private final StudentRepository studentRepository;

    public List<GroupDto> getGroupsByBranch(Long branchId) {
        return groupRepository.findByBranchId(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<StudentDto> getUnpaidStudentsByGroup(Long groupId, Integer year, Integer month) {
        Group group = groupRepository.findByIdWithStudents(groupId);
        if (group == null) {
            throw new RuntimeException("Group not found with id: " + groupId);
        }

        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        return studentRepository.findUnpaidStudentsByBranchAndMonth(group.getBranch().getId(), targetYear, targetMonth)
                .stream()
                .filter(student -> group.getStudents().contains(student))
                .map(this::convertStudentToDto)
                .collect(Collectors.toList());
    }

    public GroupDto getGroupById(Long id) {
        Group group = groupRepository.findByIdWithStudents(id);
        if (group == null) {
            throw new RuntimeException("Group not found with id: " + id);
        }
        return convertToDto(group);
    }

    public GroupDto createGroup(CreateGroupRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));
        
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));
        
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        Group group = new Group();
        group.setName(request.getName());
        group.setStartTime(request.getStartTime());
        group.setEndTime(request.getEndTime());
        group.setCourse(course);
        group.setTeacher(teacher);
        group.setBranch(branch);

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
        return convertToDto(savedGroup);
    }

    public GroupDto updateGroup(Long id, CreateGroupRequest request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));
        
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));
        
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        group.setName(request.getName());
        group.setStartTime(request.getStartTime());
        group.setEndTime(request.getEndTime());
        group.setCourse(course);
        group.setTeacher(teacher);
        group.setBranch(branch);

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
        return convertToDto(savedGroup);
    }

    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new RuntimeException("Group not found with id: " + id);
        }
        groupRepository.deleteById(id);
    }

    private GroupDto convertToDto(Group group) {
        GroupDto dto = new GroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setStartTime(group.getStartTime());
        dto.setEndTime(group.getEndTime());
        dto.setCourseId(group.getCourse().getId());
        dto.setCourseName(group.getCourse().getName());
        dto.setTeacherId(group.getTeacher().getId());
        dto.setTeacherName(group.getTeacher().getFirstName() + " " + group.getTeacher().getLastName());
        dto.setBranchId(group.getBranch().getId());
        dto.setBranchName(group.getBranch().getName());
        
        if (group.getStudents() != null) {
            List<StudentDto> studentDtos = group.getStudents().stream()
                    .map(this::convertStudentToDto)
                    .collect(Collectors.toList());
            dto.setStudents(studentDtos);
        }
        
        dto.setCreatedAt(group.getCreatedAt());
        return dto;
    }

    private StudentDto convertStudentToDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setBranchId(student.getBranch().getId());
        dto.setBranchName(student.getBranch().getName());
        dto.setCreatedAt(student.getCreatedAt());
        return dto;
    }
}