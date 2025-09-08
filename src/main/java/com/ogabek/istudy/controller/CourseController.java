package com.ogabek.istudy.controller;

import com.ogabek.istudy.dto.request.CreateCourseRequest;
import com.ogabek.istudy.dto.response.CourseDto;
import com.ogabek.istudy.security.BranchAccessControl;
import com.ogabek.istudy.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    private final CourseService courseService;
    private final BranchAccessControl branchAccessControl;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getCoursesByBranch(@RequestParam Long branchId) {
        if (!branchAccessControl.hasAccessToBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<CourseDto> courses = courseService.getCoursesByBranch(branchId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        if (!branchAccessControl.hasAccessToBranch(course.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(course);
    }

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        if (!branchAccessControl.hasAccessToBranch(request.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CourseDto course = courseService.createCourse(request);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id,
                                                  @Valid @RequestBody CreateCourseRequest request) {
        if (!branchAccessControl.hasAccessToBranch(request.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Also check access to the existing course's branch
        CourseDto existingCourse = courseService.getCourseById(id);
        if (!branchAccessControl.hasAccessToBranch(existingCourse.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CourseDto course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        if (!branchAccessControl.hasAccessToBranch(course.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDto>> searchCourses(@RequestParam Long branchId,
                                                         @RequestParam(required = false) String name) {
        if (!branchAccessControl.hasAccessToBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CourseDto> courses;
        if (name != null && !name.trim().isEmpty()) {
            courses = courseService.searchCoursesByName(branchId, name);
        } else {
            courses = courseService.getCoursesByBranch(branchId);
        }

        return ResponseEntity.ok(courses);
    }
}