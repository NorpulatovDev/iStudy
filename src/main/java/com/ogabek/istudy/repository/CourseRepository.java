package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.branch WHERE c.branch.id = :branchId")
    List<Course> findByBranchIdWithBranch(@Param("branchId") Long branchId);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.branch WHERE c.id = :id")
    Optional<Course> findByIdWithBranch(@Param("id") Long id);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.branch WHERE c.branch.id = :branchId AND c.name ILIKE %:name%")
    List<Course> findByBranchIdAndNameContainingIgnoreCaseWithBranch(@Param("branchId") Long branchId, @Param("name") String name);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.branch WHERE c.branch.id = :branchId ORDER BY c.name")
    List<Course> findByBranchIdOrderByNameWithBranch(@Param("branchId") Long branchId);

    // Keep existing methods for backward compatibility
    List<Course> findByBranchId(Long branchId);
    List<Course> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);

    @Query("SELECT c FROM Course c WHERE c.branch.id = :branchId ORDER BY c.name")
    List<Course> findByBranchIdOrderByName(@Param("branchId") Long branchId);
}