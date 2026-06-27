package com.campusbite.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for an order.
 */
@Data
public class OrderResponseDTO {
    private Long id;
    private String tokenNumber;
    private String studentName;
    private String studentEmail;
    private String status;
    private String paymentStatus;
    private Double totalAmount;
    private String pickupTime;
    private LocalDateTime orderDate;
    private List<OrderItemResponseDTO> items;
}
