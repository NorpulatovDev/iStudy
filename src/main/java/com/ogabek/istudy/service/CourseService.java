package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateCourseRequest;
import com.ogabek.istudy.dto.response.CourseDto;
import com.ogabek.istudy.dto.response.GroupDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Course;
import com.ogabek.istudy.entity.Group;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.CourseRepository;
import com.ogabek.istudy.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByBranch(Long branchId) {
        return courseRepository.findByBranchIdOrderByNameWithBranch(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDto> searchCoursesByName(Long branchId, String name) {
        return courseRepository.findByBranchIdAndNameContainingIgnoreCaseWithBranch(branchId, name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findByIdWithBranch(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return convertToDto(course);
    }

    @Transactional
    public CourseDto createCourse(CreateCourseRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setDurationMonths(request.getDurationMonths());
        course.setBranch(branch);

        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse);
    }

    @Transactional
    public CourseDto updateCourse(Long id, CreateCourseRequest request) {
        Course course = courseRepository.findByIdWithBranch(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setDurationMonths(request.getDurationMonths());
        course.setBranch(branch);

        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kurs topilmadi: " + id));

        // Check if course has groups
        List<Group> courseGroups = groupRepository.findByCourseId(id);
        if (!courseGroups.isEmpty()) {
            throw new RuntimeException("Bu kursda " + courseGroups.size() + " ta guruh mavjud. Avval guruhlarni o'chiring.");
        }

        try {
            courseRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Kursni o'chirishda xatolik yuz berdi: " + e.getMessage());
        }
    }

    private CourseDto convertToDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setDurationMonths(course.getDurationMonths());
        dto.setBranchId(course.getBranch().getId());
        dto.setBranchName(course.getBranch().getName());
        dto.setCreatedAt(course.getCreatedAt());

        // NEW: Add groups associated with this course
        List<Group> groups = groupRepository.findByCourseId(course.getId());
        List<GroupDto> groupDtos = groups.stream()
                .map(this::convertGroupToDto)
                .collect(Collectors.toList());
        dto.setGroups(groupDtos);
        return dto;
    }

    // In CourseService.java, update the convertGroupToDto method:

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