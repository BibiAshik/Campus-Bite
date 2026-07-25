package com.campusbite.service;


import com.campusbite.entity.CartItem;
import com.campusbite.entity.FoodItem;
import com.campusbite.entity.Student;
import com.campusbite.exception.ResourceNotFoundException;
import com.campusbite.repository.CartItemRepository;
import com.campusbite.repository.FoodItemRepository;
import com.campusbite.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing a student's shopping cart.
 * Methods are ordered from simplest (read) to most complex (write/transaction).
 */
@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final StudentRepository studentRepository;

    public CartItemService(CartItemRepository cartItemRepository, FoodItemRepository foodItemRepository, StudentRepository studentRepository) {
        this.cartItemRepository = cartItemRepository;
        this.foodItemRepository = foodItemRepository;
        this.studentRepository = studentRepository;
    }

    // --- Simple Read Operations ---

    public List<CartItem> getCartItems(String email) {
        Student student = getStudentByEmail(email);
        return cartItemRepository.findByStudent(student);
    }

    // --- Write / Transaction Operations ---

    @Transactional
    public CartItem addToCart(String email, Long foodItemId, int quantity) {
        Student student = getStudentByEmail(email);
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found"));

        if (foodItem.getQuantityAvailable() == null || foodItem.getQuantityAvailable() <= 0) {
            throw new IllegalArgumentException("Food item is currently out of stock");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByStudentAndFoodItem(student, foodItem);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setStudent(student);
            cartItem.setFoodItem(foodItem);
            cartItem.setQuantity(quantity);
        }

        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public CartItem updateCartItemQuantity(String email, Long cartItemId, int quantity) {
        Student student = getStudentByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Unauthorized to modify this cart item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(String email, Long cartItemId) {
        Student student = getStudentByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Unauthorized to delete this cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String email) {
        Student student = getStudentByEmail(email);
        cartItemRepository.deleteByStudent(student);
    }

    // --- Private Helper Methods ---

    private Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }
}
