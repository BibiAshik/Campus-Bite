package com.campusbite.controller;

import com.campusbite.dto.response.AdminDashboardStatsDTO;
import com.campusbite.entity.Order;
import com.campusbite.entity.Payment;
import com.campusbite.entity.PaymentStatus;
import com.campusbite.repository.OrderRepository;
import com.campusbite.repository.PaymentRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public AdminDashboardController(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Order> orders;
        List<Payment> payments;

        if (startDate != null && endDate != null) {
            orders = orderRepository.findByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate);
            payments = paymentRepository.findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(PaymentStatus.PAID, startDate, endDate);
        } else {
            orders = orderRepository.findAll();
            payments = paymentRepository.findAll().stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PAID)
                    .collect(Collectors.toList());
        }

        AdminDashboardStatsDTO stats = new AdminDashboardStatsDTO();
        
        long successfulOrdersCount = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .count();
        stats.setTotalOrders((int) successfulOrdersCount);
        
        long pendingPaidOrdersCount = orders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()) && o.getPaymentStatus() == PaymentStatus.PAID)
                .count();
        stats.setPendingOrders(pendingPaidOrdersCount);
        double revenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .mapToDouble(Order::getTotalAmount)
                .sum();
        stats.setTotalRevenue(revenue);
        
        stats.setTotalPayments(payments.size());

        return ResponseEntity.ok(stats);
    }
}
