package com.ogabek.istudy.service;

import com.ogabek.istudy.repository.ExpenseRepository;
import com.ogabek.istudy.repository.PaymentRepository;
import com.ogabek.istudy.repository.TeacherSalaryCalculationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final TeacherSalaryCalculationRepository salaryCalculationRepository;

    // Payment Reports
    public Map<String, Object> getDailyPaymentReport(Long branchId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        BigDecimal totalPayments = paymentRepository.sumPaymentsByDateRange(branchId, startOfDay, endOfDay);
        
        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("branchId", branchId);
        report.put("totalPayments", totalPayments != null ? totalPayments : BigDecimal.ZERO);
        report.put("type", "DAILY_PAYMENT");
        
        return report;
    }

    public Map<String, Object> getMonthlyPaymentReport(Long branchId, int year, int month) {
        BigDecimal totalPayments = paymentRepository.sumMonthlyPayments(branchId, year, month);
        
        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("month", month);
        report.put("branchId", branchId);
        report.put("totalPayments", totalPayments != null ? totalPayments : BigDecimal.ZERO);
        report.put("type", "MONTHLY_PAYMENT");
        
        return report;
    }

    public Map<String, Object> getPaymentRangeReport(Long branchId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        BigDecimal totalPayments = paymentRepository.sumPaymentsByDateRange(branchId, start, end);
        
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("branchId", branchId);
        report.put("totalPayments", totalPayments != null ? totalPayments : BigDecimal.ZERO);
        report.put("type", "RANGE_PAYMENT");
        
        return report;
    }

    // Expense Reports
    public Map<String, Object> getDailyExpenseReport(Long branchId, LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        BigDecimal totalExpenses = expenseRepository.sumDailyExpenses(branchId, dateTime);
        
        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("branchId", branchId);
        report.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        report.put("type", "DAILY_EXPENSE");
        
        return report;
    }

    public Map<String, Object> getMonthlyExpenseReport(Long branchId, int year, int month) {
        BigDecimal totalExpenses = expenseRepository.sumMonthlyExpenses(branchId, year, month);
        
        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("month", month);
        report.put("branchId", branchId);
        report.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        report.put("type", "MONTHLY_EXPENSE");
        
        return report;
    }

    public Map<String, Object> getExpenseRangeReport(Long branchId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        BigDecimal totalExpenses = expenseRepository.sumExpensesByDateRange(branchId, start, end);
        
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("branchId", branchId);
        report.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        report.put("type", "RANGE_EXPENSE");
        
        return report;
    }

    public Map<String, Object> getAllTimeExpenseReport(Long branchId) {
        BigDecimal totalExpenses = expenseRepository.sumAllTimeExpenses(branchId);
        
        Map<String, Object> report = new HashMap<>();
        report.put("branchId", branchId);
        report.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        report.put("type", "ALL_TIME_EXPENSE");
        
        return report;
    }

    // Salary Reports
    public Map<String, Object> getMonthlySalaryReport(Long branchId, int year, int month) {
        BigDecimal totalSalaries = salaryCalculationRepository.sumMonthlySalaries(branchId, year, month);
        
        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("month", month);
        report.put("branchId", branchId);
        report.put("totalSalaries", totalSalaries != null ? totalSalaries : BigDecimal.ZERO);
        report.put("type", "MONTHLY_SALARY");
        
        return report;
    }

    public Map<String, Object> getSalaryRangeReport(Long branchId, int startYear, int startMonth, 
                                                    int endYear, int endMonth) {
        BigDecimal totalSalaries = salaryCalculationRepository
                .sumSalariesByRange(branchId, startYear, startMonth, endYear, endMonth);
        
        Map<String, Object> report = new HashMap<>();
        report.put("startYear", startYear);
        report.put("startMonth", startMonth);
        report.put("endYear", endYear);
        report.put("endMonth", endMonth);
        report.put("branchId", branchId);
        report.put("totalSalaries", totalSalaries != null ? totalSalaries : BigDecimal.ZERO);
        report.put("type", "RANGE_SALARY");
        
        return report;
    }

    // Combined Financial Report
    public Map<String, Object> getFinancialSummary(Long branchId, int year, int month) {
        BigDecimal totalPayments = paymentRepository.sumMonthlyPayments(branchId, year, month);
        BigDecimal totalExpenses = expenseRepository.sumMonthlyExpenses(branchId, year, month);
        BigDecimal totalSalaries = salaryCalculationRepository.sumMonthlySalaries(branchId, year, month);
        
        BigDecimal totalIncome = totalPayments != null ? totalPayments : BigDecimal.ZERO;
        BigDecimal totalCosts = (totalExpenses != null ? totalExpenses : BigDecimal.ZERO)
                .add(totalSalaries != null ? totalSalaries : BigDecimal.ZERO);
        BigDecimal netProfit = totalIncome.subtract(totalCosts);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("year", year);
        summary.put("month", month);
        summary.put("branchId", branchId);
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        summary.put("totalSalaries", totalSalaries != null ? totalSalaries : BigDecimal.ZERO);
        summary.put("totalCosts", totalCosts);
        summary.put("netProfit", netProfit);
        summary.put("type", "FINANCIAL_SUMMARY");
        
        return summary;
    }

    public Map<String, Object> getFinancialSummaryRange(Long branchId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        BigDecimal totalPayments = paymentRepository.sumPaymentsByDateRange(branchId, start, end);
        BigDecimal totalExpenses = expenseRepository.sumExpensesByDateRange(branchId, start, end);
        
        // For salary range, we need to convert dates to year/month
        int startYear = startDate.getYear();
        int startMonth = startDate.getMonthValue();
        int endYear = endDate.getYear();
        int endMonth = endDate.getMonthValue();
        
        BigDecimal totalSalaries = salaryCalculationRepository
                .sumSalariesByRange(branchId, startYear, startMonth, endYear, endMonth);
        
        BigDecimal totalIncome = totalPayments != null ? totalPayments : BigDecimal.ZERO;
        BigDecimal totalCosts = (totalExpenses != null ? totalExpenses : BigDecimal.ZERO)
                .add(totalSalaries != null ? totalSalaries : BigDecimal.ZERO);
        BigDecimal netProfit = totalIncome.subtract(totalCosts);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("branchId", branchId);
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        summary.put("totalSalaries", totalSalaries != null ? totalSalaries : BigDecimal.ZERO);
        summary.put("totalCosts", totalCosts);
        summary.put("netProfit", netProfit);
        summary.put("type", "FINANCIAL_SUMMARY_RANGE");
        
        return summary;
    }
}
