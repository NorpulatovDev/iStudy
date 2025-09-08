package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findByBranchId(Long branchId);
    List<Teacher> findByBranchIdAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            Long branchId, String firstName, String lastName);
    
    @Query("SELECT t FROM Teacher t WHERE t.branch.id = :branchId AND " +
           "(LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Teacher> findByBranchIdAndFullName(@Param("branchId") Long branchId, @Param("name") String name);
}
