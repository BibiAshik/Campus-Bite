package com.campusbite.controller;

import com.campusbite.dto.request.PaymentCreateRequestDTO;
import com.campusbite.dto.request.PaymentVerifyRequestDTO;
import com.campusbite.dto.response.PaymentResponseDTO;
import com.campusbite.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Student creates a Razorpay order.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentCreateRequestDTO request, Authentication authentication) {
        String studentEmail = authentication.getName();
        PaymentResponseDTO payment = paymentService.createRazorpayOrder(request.getOrderId(), studentEmail);
        return ResponseEntity.ok(payment);
    }

    /**
     * Student verifies Razorpay payment after success on client.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDTO> verifyPayment(@Valid @RequestBody PaymentVerifyRequestDTO request, Authentication authentication) {
        String studentEmail = authentication.getName();
        PaymentResponseDTO payment = paymentService.verifyAndCompletePayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                studentEmail
        );
        return ResponseEntity.ok(payment);
    }

    /**
     * Student gets their own payments.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-payments")
    public List<PaymentResponseDTO> getMyPayments(
            @RequestParam(required = false) String searchId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            Authentication authentication) {
        String studentEmail = authentication.getName();
        return paymentService.getPaymentsByStudentEmail(studentEmail, searchId, startDate, endDate);
    }

    /**
     * Admin gets all payments, filtered by date.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<PaymentResponseDTO> getAllPayments(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        return paymentService.getAllPayments(startDate, endDate);
    }
}
