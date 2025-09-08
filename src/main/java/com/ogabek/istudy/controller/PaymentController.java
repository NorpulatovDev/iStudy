package com.ogabek.istudy.controller;

import com.ogabek.istudy.dto.request.CreatePaymentRequest;
import com.ogabek.istudy.dto.response.PaymentDto;
import com.ogabek.istudy.security.BranchAccessControl;
import com.ogabek.istudy.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    private final PaymentService paymentService;
    private final BranchAccessControl branchAccessControl;

    @GetMapping
    public ResponseEntity<List<PaymentDto>> getPaymentsByBranch(@RequestParam Long branchId) {
        if (!branchAccessControl.hasAccessToBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<PaymentDto> payments = paymentService.getPaymentsByBranch(branchId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByStudent(@PathVariable Long studentId) {
        List<PaymentDto> payments = paymentService.getPaymentsByStudent(studentId);
        // Check access to the first payment's branch (all payments should be from same branch)
        if (!payments.isEmpty() && !branchAccessControl.hasAccessToBranch(payments.get(0).getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        PaymentDto payment = paymentService.getPaymentById(id);
        if (!branchAccessControl.hasAccessToBranch(payment.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(payment);
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        if (!branchAccessControl.hasAccessToBranch(request.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PaymentDto payment = paymentService.createPayment(request);
        return ResponseEntity.ok(payment);
    }
}