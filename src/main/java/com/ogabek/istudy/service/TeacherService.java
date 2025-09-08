package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateTeacherRequest;
import com.ogabek.istudy.dto.response.TeacherDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.SalaryType;
import com.ogabek.istudy.entity.Teacher;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final BranchRepository branchRepository;

    public List<TeacherDto> getTeachersByBranch(Long branchId) {
        return teacherRepository.findByBranchId(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TeacherDto> searchTeachersByName(Long branchId, String name) {
        return teacherRepository.findByBranchIdAndFullName(branchId, name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TeacherDto> getTeachersBySalaryType(Long branchId, String salaryType) {
        List<Teacher> teachers = teacherRepository.findByBranchId(branchId);
        return teachers.stream()
                .filter(teacher -> teacher.getSalaryType().name().equals(salaryType.toUpperCase()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TeacherDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
        return convertToDto(teacher);
    }

    public TeacherDto createTeacher(CreateTeacherRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setPhoneNumber(request.getPhoneNumber());
        teacher.setBaseSalary(request.getBaseSalary());
        teacher.setPaymentPercentage(request.getPaymentPercentage());
        teacher.setSalaryType(request.getSalaryType());
        teacher.setBranch(branch);

        Teacher savedTeacher = teacherRepository.save(teacher);
        return convertToDto(savedTeacher);
    }

    public TeacherDto updateTeacher(Long id, CreateTeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setPhoneNumber(request.getPhoneNumber());
        teacher.setBaseSalary(request.getBaseSalary());
        teacher.setPaymentPercentage(request.getPaymentPercentage());
        teacher.setSalaryType(request.getSalaryType());
        teacher.setBranch(branch);

        Teacher savedTeacher = teacherRepository.save(teacher);
        return convertToDto(savedTeacher);
    }

    public void deleteTeacher(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new RuntimeException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    private TeacherDto convertToDto(Teacher teacher) {
        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setFirstName(teacher.getFirstName());
        dto.setLastName(teacher.getLastName());
        dto.setPhoneNumber(teacher.getPhoneNumber());
        dto.setEmail(teacher.getEmail());
        dto.setBaseSalary(teacher.getBaseSalary());
        dto.setPaymentPercentage(teacher.getPaymentPercentage());
        dto.setSalaryType(teacher.getSalaryType().name());
        dto.setBranchId(teacher.getBranch().getId());
        dto.setBranchName(teacher.getBranch().getName());
        dto.setCreatedAt(teacher.getCreatedAt());
        return dto;
    }
}