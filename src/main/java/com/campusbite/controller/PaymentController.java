package com.campusbite.controller;

import com.campusbite.dto.request.PaymentCreateRequestDTO;
import com.campusbite.dto.request.PaymentVerifyRequestDTO;
import com.campusbite.dto.response.PaymentResponseDTO;
import com.campusbite.entity.Payment;
import com.campusbite.mapper.PaymentMapper;
import com.campusbite.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    /**
     * Student creates a Razorpay order.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentCreateRequestDTO request, Authentication authentication) {
        String studentEmail = authentication.getName();
        Payment payment = paymentService.createPaymentOrder(studentEmail, request);
        return ResponseEntity.ok(paymentMapper.toResponseDTO(payment));
    }

    /**
     * Student verifies Razorpay payment after success on client.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDTO> verifyPayment(@Valid @RequestBody PaymentVerifyRequestDTO request, Authentication authentication) {
        String studentEmail = authentication.getName();
        Payment payment = paymentService.verifyAndCompletePayment(studentEmail, request);
        return ResponseEntity.ok(paymentMapper.toResponseDTO(payment));
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
        return paymentService.getPaymentsByStudentEmail(studentEmail, searchId, startDate, endDate)
                .stream()
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Admin gets all payments, filtered by date.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<PaymentResponseDTO> getAllPayments(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        return paymentService.getAllPayments(startDate, endDate)
                .stream()
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
