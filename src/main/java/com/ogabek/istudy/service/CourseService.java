package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateCourseRequest;
import com.ogabek.istudy.dto.response.CourseDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Course;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;

    public List<CourseDto> getCoursesByBranch(Long branchId) {
        return courseRepository.findByBranchIdOrderByName(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return convertToDto(course);
    }

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

    public CourseDto updateCourse(Long id, CreateCourseRequest request) {
        Course course = courseRepository.findById(id)
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

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
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
        return dto;
    }
}