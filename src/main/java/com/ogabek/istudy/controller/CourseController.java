package com.ogabek.istudy.controller;

import com.ogabek.istudy.dto.request.CreateCourseRequest;
import com.ogabek.istudy.dto.request.CreateStudentRequest;
import com.ogabek.istudy.dto.response.CourseDto;
import com.ogabek.istudy.dto.response.StudentDto;
import com.ogabek.istudy.security.BranchAccessControl;
import com.ogabek.istudy.service.CourseService;
import com.ogabek.istudy.service.StudentService;
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
    private final StudentService studentService;

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
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        if (!branchAccessControl.hasAccessToBranch(request.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        StudentDto student = studentService.createStudent(request);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable Long id, 
                                                    @Valid @RequestBody CreateStudentRequest request) {
        if (!branchAccessControl.hasAccessToBranch(request.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        StudentDto student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        StudentDto student = studentService.getStudentById(id);
        if (!branchAccessControl.hasAccessToBranch(student.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }
}