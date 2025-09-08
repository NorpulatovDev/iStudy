package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Payment;
import com.ogabek.istudy.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
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
}