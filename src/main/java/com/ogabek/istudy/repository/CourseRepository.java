package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByBranchId(Long branchId);
    List<Course> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);
    
    @Query("SELECT c FROM Course c WHERE c.branch.id = :branchId ORDER BY c.name")
    List<Course> findByBranchIdOrderByName(@Param("branchId") Long branchId);
}