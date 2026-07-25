package com.campusbite.controller;

import com.campusbite.dto.request.OrderCreateRequestDTO;
import com.campusbite.dto.response.OrderResponseDTO;
import java.security.Principal;
import com.campusbite.entity.Order;
import com.campusbite.mapper.OrderMapper;
import com.campusbite.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    /**
     * Student places a new order.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(Principal principal, @Valid @RequestBody OrderCreateRequestDTO request) {
        String studentEmail = principal.getName(); // JWT subject is email
        Order order = orderService.placeOrderForStudent(studentEmail, request);
        return ResponseEntity.ok(orderMapper.toResponseDTO(order));
    }

    /**
     * Public endpoint for tracking an order by token.
     */
    @GetMapping("/track/{token}")
    public ResponseEntity<OrderResponseDTO> trackOrder(@PathVariable String token) {
        Order order = orderService.getOrderByToken(token);
        if (order == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(orderMapper.toResponseDTO(order));
    }

    /**
     * Student gets their own orders.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-orders")
    public List<OrderResponseDTO> getMyOrders(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            Authentication authentication) {
        String studentEmail = authentication.getName();
        return orderService.getOrdersByStudentEmail(studentEmail, status, startDate, endDate)
                .stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Admin gets all orders, optionally filtered by status and date.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<OrderResponseDTO> getAllOrders(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        return orderService.getOrdersByStatusAndDate(status, startDate, endDate)
                .stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Admin gets pending orders.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public List<OrderResponseDTO> getPendingOrders() {
        return orderService.getOrdersByStatusAndDate("PENDING", null, null)
                .stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Admin updates order status.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(orderMapper.toResponseDTO(order));
    }
}
