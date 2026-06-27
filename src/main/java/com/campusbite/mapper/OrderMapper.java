package com.campusbite.mapper;

import com.campusbite.dto.response.OrderItemResponseDTO;
import com.campusbite.dto.response.OrderResponseDTO;
import com.campusbite.entity.Order;
import com.campusbite.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setTokenNumber(order.getTokenNumber());
        dto.setStudentName(order.getStudentName());
        dto.setStudentEmail(order.getStudentEmail());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPickupTime(order.getPickupTime());
        dto.setOrderDate(order.getOrderDate());
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(this::toItemResponseDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(item.getId());
        dto.setFoodItemName(item.getFoodItem().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}
