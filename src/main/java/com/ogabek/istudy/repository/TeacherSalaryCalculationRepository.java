package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.SalaryCalculationStatus;
import com.ogabek.istudy.entity.TeacherSalaryCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSalaryCalculationRepository extends JpaRepository<TeacherSalaryCalculation, Long> {
    List<TeacherSalaryCalculation> findByBranchId(Long branchId);
    List<TeacherSalaryCalculation> findByTeacherId(Long teacherId);
    List<TeacherSalaryCalculation> findByBranchIdAndYearAndMonth(Long branchId, int year, int month);
    Optional<TeacherSalaryCalculation> findByTeacherIdAndYearAndMonth(Long teacherId, int year, int month);
    
    // Monthly salary sum for all teachers
    @Query("SELECT COALESCE(SUM(tsc.totalSalary), 0) FROM TeacherSalaryCalculation tsc " +
           "WHERE tsc.branch.id = :branchId AND tsc.year = :year AND tsc.month = :month")
    BigDecimal sumMonthlySalaries(@Param("branchId") Long branchId, @Param("year") int year, @Param("month") int month);
    
    // Range salary sum
    @Query("SELECT COALESCE(SUM(tsc.totalSalary), 0) FROM TeacherSalaryCalculation tsc " +
           "WHERE tsc.branch.id = :branchId AND " +
           "((tsc.year = :startYear AND tsc.month >= :startMonth) OR (tsc.year > :startYear)) AND " +
           "((tsc.year = :endYear AND tsc.month <= :endMonth) OR (tsc.year < :endYear))")
    BigDecimal sumSalariesByRange(@Param("branchId") Long branchId, 
                                 @Param("startYear") int startYear, @Param("startMonth") int startMonth,
                                 @Param("endYear") int endYear, @Param("endMonth") int endMonth);
}