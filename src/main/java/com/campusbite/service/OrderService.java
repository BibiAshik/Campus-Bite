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
import com.campusbite.repository.FoodItemRepository;
import com.campusbite.repository.OrderRepository;
import com.campusbite.repository.StudentRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing student orders.
 * Methods are ordered from simplest (read) to most complex (multi-step transactional write).
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodItemRepository foodItemRepository;
    private final StudentRepository studentRepository;

    public OrderService(OrderRepository orderRepository, FoodItemRepository foodItemRepository, StudentRepository studentRepository) {
        this.orderRepository = orderRepository;
        this.foodItemRepository = foodItemRepository;
        this.studentRepository = studentRepository;
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
    public List<Order> getOrdersByStudentEmail(String studentEmail, String status, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            if ("ALL".equalsIgnoreCase(status)) {
                return orderRepository.findByStudentEmailAndOrderDateBetweenOrderByOrderDateDesc(studentEmail, startDate, endDate);
            } else {
                return orderRepository.findByStudentEmailAndStatusAndOrderDateBetweenOrderByOrderDateDesc(studentEmail, status, startDate, endDate);
            }
        } else {
            if ("ALL".equalsIgnoreCase(status)) {
                return orderRepository.findByStudentEmailOrderByOrderDateDesc(studentEmail);
            } else {
                return orderRepository.findByStudentEmailAndStatusOrderByOrderDateDesc(studentEmail, status);
            }
        }
    }

    /**
     * Purpose: Get orders by status and date range (used by Admin).
     */
    public List<Order> getOrdersByStatusAndDate(String status, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            if ("ALL".equalsIgnoreCase(status)) {
                return orderRepository.findByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate);
            } else {
                return orderRepository.findByStatusAndOrderDateBetweenOrderByOrderDateDesc(status, startDate, endDate);
            }
        } else {
            if ("ALL".equalsIgnoreCase(status)) {
                return orderRepository.findAllByOrderByOrderDateDesc();
            } else {
                return orderRepository.findByStatusOrderByOrderDateDesc(status);
            }
        }
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
     */
    @Transactional
    public Order placeOrderForStudent(String studentEmail, OrderCreateRequestDTO request) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Order order = new Order();
        order.setStudentName(student.getName());
        order.setStudentEmail(student.getEmail());
        order.setRollNumber(""); // Not used anymore as it's auto-filled
        order.setPickupTime(request.getPickupTime());
        order.setStatus("PENDING");
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        // Generate a unique token number
        String token = "TKN-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        order.setTokenNumber(token);

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDTO itemReq : request.getItems()) {
            FoodItem foodItem = foodItemRepository.findById(itemReq.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food item not found: " + itemReq.getFoodItemId()));
            
            if (foodItem.getQuantityAvailable() < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Not enough quantity available for " + foodItem.getName());
            }

            // Deduct quantity
            foodItem.setQuantityAvailable(foodItem.getQuantityAvailable() - itemReq.getQuantity());
            
            try {
                foodItemRepository.saveAndFlush(foodItem);
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new IllegalArgumentException("Sorry, the item '" + foodItem.getName() + "' was just purchased by someone else!");
            }

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

        return orderRepository.save(order);
    }
}

