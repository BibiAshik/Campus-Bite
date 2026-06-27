package com.campusbite.service;

import com.campusbite.dto.request.OrderCreateRequestDTO;
import com.campusbite.dto.request.OrderItemRequestDTO;
import com.campusbite.dto.response.OrderResponseDTO;
import com.campusbite.entity.FoodItem;
import com.campusbite.entity.Order;
import com.campusbite.entity.OrderItem;
import com.campusbite.entity.PaymentStatus;
import com.campusbite.entity.Student;
import com.campusbite.exception.ResourceNotFoundException;
import com.campusbite.mapper.OrderMapper;
import com.campusbite.repository.FoodItemRepository;
import com.campusbite.repository.OrderRepository;
import com.campusbite.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing student orders.
 * Methods are ordered from simplest (read) to most complex (multi-step transactional write).
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodItemRepository foodItemRepository;
    private final StudentRepository studentRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, FoodItemRepository foodItemRepository, StudentRepository studentRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.foodItemRepository = foodItemRepository;
        this.studentRepository = studentRepository;
        this.orderMapper = orderMapper;
    }

    // --- Simple Read Operations ---

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatusOrderByOrderDateDesc("PENDING");
    }

    public Order getOrderByToken(String tokenNumber) {
        return orderRepository.findByTokenNumber(tokenNumber).orElse(null);
    }

    /**
     * Purpose: Get orders for a specific student, optionally filtered by status and dates.
     */
    public List<OrderResponseDTO> getOrdersByStudentEmail(String studentEmail, String status, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders;
        if (startDate != null && endDate != null) {
            if ("ALL".equalsIgnoreCase(status)) {
                orders = orderRepository.findByStudentEmailAndOrderDateBetweenOrderByOrderDateDesc(studentEmail, startDate, endDate);
            } else {
                orders = orderRepository.findByStudentEmailAndStatusAndOrderDateBetweenOrderByOrderDateDesc(studentEmail, status, startDate, endDate);
            }
        } else {
            if ("ALL".equalsIgnoreCase(status)) {
                orders = orderRepository.findByStudentEmailOrderByOrderDateDesc(studentEmail);
            } else {
                orders = orderRepository.findByStudentEmailAndStatusOrderByOrderDateDesc(studentEmail, status);
            }
        }
        
        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Purpose: Get orders by status and date range (used by Admin).
     */
    public List<OrderResponseDTO> getOrdersByStatusAndDate(String status, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders;
        if (startDate != null && endDate != null) {
            if ("ALL".equalsIgnoreCase(status)) {
                orders = orderRepository.findByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate);
            } else {
                orders = orderRepository.findByStatusAndOrderDateBetweenOrderByOrderDateDesc(status, startDate, endDate);
            }
        } else {
            if ("ALL".equalsIgnoreCase(status)) {
                orders = orderRepository.findAllByOrderByOrderDateDesc();
            } else {
                orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
            }
        }
        return orders.stream().map(orderMapper::toResponseDTO).collect(Collectors.toList());
    }

    // --- Simple Write Operations ---

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // --- Complex Transactional Operations ---

    /**
     * Purpose: Places an order for the authenticated student.
     * This is the most complex method — it validates stock, deducts quantities,
     * builds order items, calculates the total, and saves the full order in one transaction.
     * Input: OrderCreateRequestDTO and studentEmail from JWT.
     * Output: OrderResponseDTO
     */
    @Transactional
    public OrderResponseDTO placeOrderForStudent(OrderCreateRequestDTO dto, String studentEmail) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Order order = new Order();
        order.setStudentName(student.getName());
        order.setStudentEmail(student.getEmail());
        order.setRollNumber(""); // Not used anymore as it's auto-filled
        order.setPickupTime(dto.getPickupTime());
        order.setStatus("PENDING");
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        // Generate a simple token number like A-047
        String token = "A-" + String.format("%03d", (int)(Math.random() * 1000));
        order.setTokenNumber(token);

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDTO itemReq : dto.getItems()) {
            FoodItem foodItem = foodItemRepository.findById(itemReq.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food item not found: " + itemReq.getFoodItemId()));
            
            if (foodItem.getQuantityAvailable() < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough quantity available for " + foodItem.getName());
            }

            // Deduct quantity
            foodItem.setQuantityAvailable(foodItem.getQuantityAvailable() - itemReq.getQuantity());
            foodItemRepository.save(foodItem);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFoodItem(foodItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(foodItem.getPrice() * itemReq.getQuantity());
            
            totalAmount += orderItem.getPrice();
            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponseDTO(saved);
    }
}

