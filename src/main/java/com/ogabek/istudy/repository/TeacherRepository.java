package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Teacher;
import com.ogabek.istudy.entity.SalaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    // Fetch teachers with branch eagerly loaded
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.branch WHERE t.branch.id = :branchId")
    List<Teacher> findByBranchIdWithBranch(@Param("branchId") Long branchId);

    // Fetch single teacher with branch eagerly loaded
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.branch WHERE t.id = :id")
    Optional<Teacher> findByIdWithBranch(@Param("id") Long id);

    // Search by name with branch eagerly loaded
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.branch WHERE t.branch.id = :branchId AND " +
            "(LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Teacher> findByBranchIdAndFullNameWithBranch(@Param("branchId") Long branchId, @Param("name") String name);

    // Find by salary type with branch eagerly loaded
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.branch WHERE t.branch.id = :branchId AND t.salaryType = :salaryType")
    List<Teacher> findByBranchIdAndSalaryTypeWithBranch(@Param("branchId") Long branchId, @Param("salaryType") SalaryType salaryType);

    // Keep the original methods for backward compatibility (these may still cause lazy loading issues)
    List<Teacher> findByBranchId(Long branchId);
    List<Teacher> findByBranchIdAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            Long branchId, String firstName, String lastName);

    @Query("SELECT t FROM Teacher t WHERE t.branch.id = :branchId AND " +
            "(LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Teacher> findByBranchIdAndFullName(@Param("branchId") Long branchId, @Param("name") String name);
}