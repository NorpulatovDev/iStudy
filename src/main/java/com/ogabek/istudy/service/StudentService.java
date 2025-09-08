package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateStudentRequest;
import com.ogabek.istudy.dto.response.GroupDto;
import com.ogabek.istudy.dto.response.PaymentDto;
import com.ogabek.istudy.dto.response.StudentDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Student;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.GroupRepository;
import com.ogabek.istudy.repository.PaymentRepository;
import com.ogabek.istudy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final BranchRepository branchRepository;
    private final PaymentRepository paymentRepository;
    private final GroupRepository groupRepository;

    public List<StudentDto> getStudentsByBranch(Long branchId) {
        return studentRepository.findByBranchId(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<StudentDto> getUnpaidStudents(Long branchId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        
        return studentRepository.findUnpaidStudentsByBranchAndMonth(branchId, targetYear, targetMonth)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<StudentDto> searchStudentsByName(Long branchId, String name) {
        return studentRepository.findByBranchIdAndFullName(branchId, name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getStudentPaymentHistory(Long studentId) {
        return paymentRepository.findByStudentId(studentId).stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    public List<GroupDto> getStudentGroups(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        
        // This is a simplified version - you might need to adjust based on your Group entity structure
        return groupRepository.findByBranchId(student.getBranch().getId()).stream()
                .filter(group -> group.getStudents() != null && group.getStudents().contains(student))
                .map(this::convertGroupToDto)
                .collect(Collectors.toList());
    }

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

    public List<StudentDto> getRecentStudents(Long branchId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return studentRepository.findByBranchId(branchId).stream()
                .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        return convertToDto(student);
    }

    public StudentDto createStudent(CreateStudentRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setBranch(branch);

        Student savedStudent = studentRepository.save(student);
        return convertToDto(savedStudent);
    }

    public StudentDto updateStudent(Long id, CreateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setBranch(branch);

        Student savedStudent = studentRepository.save(student);
        return convertToDto(savedStudent);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    private StudentDto convertToDto(Student student) {
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

    private PaymentDto convertPaymentToDto(com.ogabek.istudy.entity.Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setStudentId(payment.getStudent().getId());
        dto.setStudentName(payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName());
        dto.setCourseId(payment.getCourse().getId());
        dto.setCourseName(payment.getCourse().getName());
        dto.setAmount(payment.getAmount());
        dto.setDescription(payment.getDescription());
        dto.setStatus(payment.getStatus().name());
        dto.setBranchId(payment.getBranch().getId());
        dto.setBranchName(payment.getBranch().getName());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

    private GroupDto convertGroupToDto(com.ogabek.istudy.entity.Group group) {
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
        dto.setCreatedAt(group.getCreatedAt());
        return dto;
    }
}