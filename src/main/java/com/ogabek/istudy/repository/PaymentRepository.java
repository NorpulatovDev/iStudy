package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Payment;
import com.ogabek.istudy.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Fetch payments with all related entities eagerly loaded
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.student " +
            "LEFT JOIN FETCH p.course " +
            "LEFT JOIN FETCH p.branch " +
            "WHERE p.branch.id = :branchId " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByBranchIdWithAllRelations(@Param("branchId") Long branchId);

    // Fetch payments by student with relations
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.student " +
            "LEFT JOIN FETCH p.course " +
            "LEFT JOIN FETCH p.branch " +
            "WHERE p.student.id = :studentId " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByStudentIdWithRelations(@Param("studentId") Long studentId);

    // Fetch single payment with all relations
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.student " +
            "LEFT JOIN FETCH p.course " +
            "LEFT JOIN FETCH p.branch " +
            "WHERE p.id = :id")
    Optional<Payment> findByIdWithAllRelations(@Param("id") Long id);

    // Fetch payments by date range with relations
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.student " +
            "LEFT JOIN FETCH p.course " +
            "LEFT JOIN FETCH p.branch " +
            "WHERE p.branch.id = :branchId AND p.createdAt BETWEEN :start AND :end " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByBranchIdAndCreatedAtBetweenWithRelations(@Param("branchId") Long branchId,
                                                                 @Param("start") LocalDateTime start,
                                                                 @Param("end") LocalDateTime end);

    // Fetch payments by year and month with relations
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.student " +
            "LEFT JOIN FETCH p.course " +
            "LEFT JOIN FETCH p.branch " +
            "WHERE p.branch.id = :branchId AND p.paymentYear = :year AND p.paymentMonth = :month " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByBranchIdAndPaymentYearAndPaymentMonthWithRelations(@Param("branchId") Long branchId,
                                                                           @Param("year") int year,
                                                                           @Param("month") int month);

    // Keep original methods for backward compatibility (may cause lazy loading issues)
    List<Payment> findByBranchId(Long branchId);
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByBranchIdAndCreatedAtBetween(Long branchId, LocalDateTime start, LocalDateTime end);
    List<Payment> findByBranchIdAndPaymentYearAndPaymentMonth(Long branchId, int year, int month);

    // Daily payments sum
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.branch.id = :branchId AND " +
            "DATE(p.createdAt) = DATE(:date)")
    BigDecimal sumDailyPayments(@Param("branchId") Long branchId, @Param("date") LocalDateTime date);

    // Monthly payments sum
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.branch.id = :branchId AND " +
            "p.paymentYear = :year AND p.paymentMonth = :month")
    BigDecimal sumMonthlyPayments(@Param("branchId") Long branchId, @Param("year") int year, @Param("month") int month);

    // Range payments sum
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.branch.id = :branchId AND " +
            "p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumPaymentsByDateRange(@Param("branchId") Long branchId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // Teacher's student payments for salary calculation
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN Group g ON p.student MEMBER OF g.students " +
            "WHERE g.teacher.id = :teacherId AND p.paymentYear = :year AND p.paymentMonth = :month")
    BigDecimal sumTeacherStudentPayments(@Param("teacherId") Long teacherId,
                                         @Param("year") int year, @Param("month") int month);

    // Count teacher's students who paid
    @Query("SELECT COUNT(DISTINCT p.student.id) FROM Payment p JOIN Group g ON p.student MEMBER OF g.students " +
            "WHERE g.teacher.id = :teacherId AND p.paymentYear = :year AND p.paymentMonth = :month")
    int countTeacherPaidStudents(@Param("teacherId") Long teacherId,
                                 @Param("year") int year, @Param("month") int month);


    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.student.id = :studentId AND p.course.id = :courseId " +
            "AND p.paymentYear = :year AND p.paymentMonth = :month")
    BigDecimal getTotalPaidByStudentForCourseInMonth(@Param("studentId") Long studentId,
                                                     @Param("courseId") Long courseId,
                                                     @Param("year") int year,
                                                     @Param("month") int month);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.student.id = :studentId AND p.group.id = :groupId " +
            "AND p.paymentYear = :year AND p.paymentMonth = :month")
    BigDecimal getTotalPaidByStudentInGroupForMonth(@Param("studentId") Long studentId,
                                                    @Param("groupId") Long groupId,
                                                    @Param("year") int year,
                                                    @Param("month") int month);

    List<Payment> findByCourseId(Long courseId);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}