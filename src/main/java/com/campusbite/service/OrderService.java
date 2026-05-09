package com.campusbite.service;

import com.campusbite.dto.OrderItemRequest;
import com.campusbite.dto.OrderRequest;
import com.campusbite.entity.FoodItem;
import com.campusbite.entity.Order;
import com.campusbite.entity.OrderItem;
import com.campusbite.repository.FoodItemRepository;
import com.campusbite.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodItemRepository foodItemRepository;

    public OrderService(OrderRepository orderRepository, FoodItemRepository foodItemRepository) {
        this.orderRepository = orderRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setStudentName(request.getStudentName());
        order.setRollNumber(request.getRollNumber());
        order.setPickupTime(request.getPickupTime());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        
        // Generate a simple token number like A-047
        String token = "A-" + String.format("%03d", (int)(Math.random() * 1000));
        order.setTokenNumber(token);

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            FoodItem foodItem = foodItemRepository.findById(itemReq.getFoodItemId())
                    .orElseThrow(() -> new RuntimeException("Food item not found"));
            
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

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatusOrderByOrderDateDesc("PENDING");
    }
    
    public Order getOrderByToken(String tokenNumber) {
        return orderRepository.findByTokenNumber(tokenNumber).orElse(null);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
