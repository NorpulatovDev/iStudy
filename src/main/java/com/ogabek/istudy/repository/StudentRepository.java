package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // NEW: Get total amount paid by student in specific month/year
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.student.id = :studentId " +
            "AND p.paymentYear = :year AND p.paymentMonth = :month")
    BigDecimal getTotalPaidByStudentInMonth(@Param("studentId") Long studentId,
                                            @Param("year") int year, @Param("month") int month);

    // NEW: Get last payment date for student
    @Query("SELECT MAX(p.createdAt) FROM Payment p WHERE p.student.id = :studentId")
    LocalDateTime getLastPaymentDate(@Param("studentId") Long studentId);

    // NEW: Check if student has paid in specific month/year
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.student.id = :studentId " +
            "AND p.paymentYear = :year AND p.paymentMonth = :month")
    Boolean hasStudentPaidInMonth(@Param("studentId") Long studentId,
                                  @Param("year") int year, @Param("month") int month);

    // NEW: Get expected monthly payment for student (based on courses they're enrolled in)
    @Query("SELECT COALESCE(SUM(c.price), 0) FROM Course c JOIN Group g ON g.course = c " +
            "JOIN g.students s WHERE s.id = :studentId")
    BigDecimal getExpectedMonthlyPaymentForStudent(@Param("studentId") Long studentId);

    // NEW: Get students with payment details for specific month
    @Query("""
    SELECT s FROM Student s 
    LEFT JOIN FETCH s.branch 
    WHERE s.branch.id = :branchId 
    ORDER BY s.lastName ASC, s.firstName ASC
    """)
    List<Student> findByBranchIdWithBranch(@Param("branchId") Long branchId);
}