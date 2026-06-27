package com.campusbite.service;

import com.campusbite.dto.request.CartItemRequestDTO;
import com.campusbite.dto.response.CartItemResponseDTO;
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

    public List<CartItemResponseDTO> getCartItems(String email) {
        Student student = getStudentByEmail(email);
        return cartItemRepository.findByStudent(student).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Write / Transaction Operations ---

    @Transactional
    public CartItemResponseDTO addToCart(String email, CartItemRequestDTO request) {
        Student student = getStudentByEmail(email);
        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found"));

        if (foodItem.getQuantityAvailable() == null || foodItem.getQuantityAvailable() <= 0) {
            throw new RuntimeException("Food item is currently out of stock");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByStudentAndFoodItem(student, foodItem);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setStudent(student);
            cartItem.setFoodItem(foodItem);
            cartItem.setQuantity(request.getQuantity());
        }

        return mapToResponseDTO(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponseDTO updateCartItemQuantity(String email, Long cartItemId, int quantity) {
        Student student = getStudentByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized to modify this cart item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(quantity);
        return mapToResponseDTO(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeFromCart(String email, Long cartItemId) {
        Student student = getStudentByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized to delete this cart item");
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

    private CartItemResponseDTO mapToResponseDTO(CartItem item) {
        FoodItem food = item.getFoodItem();
        return CartItemResponseDTO.builder()
                .id(item.getId())
                .foodItemId(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .price(food.getPrice())
                .imageUrl(food.getImageUrl())
                .isVeg(food.getIsVeg())
                .quantity(item.getQuantity())
                .totalItemPrice(food.getPrice() * item.getQuantity())
                .build();
    }
}
