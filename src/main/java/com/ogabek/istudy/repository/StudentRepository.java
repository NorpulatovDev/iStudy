package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByBranchId(Long branchId);
    List<Student> findByBranchIdAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            Long branchId, String firstName, String lastName);
    
    @Query("SELECT s FROM Student s WHERE s.branch.id = :branchId AND " +
           "(LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Student> findByBranchIdAndFullName(@Param("branchId") Long branchId, @Param("name") String name);
    
    // Find students who haven't paid for specific month/year
    @Query("SELECT s FROM Student s WHERE s.branch.id = :branchId AND s.id NOT IN " +
           "(SELECT DISTINCT p.student.id FROM Payment p WHERE p.paymentYear = :year AND p.paymentMonth = :month)")
    List<Student> findUnpaidStudentsByBranchAndMonth(@Param("branchId") Long branchId, 
                                                    @Param("year") int year, @Param("month") int month);
}
