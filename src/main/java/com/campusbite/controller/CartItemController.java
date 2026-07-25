package com.campusbite.controller;

import com.campusbite.dto.request.CartItemRequestDTO;
import com.campusbite.dto.response.CartItemResponseDTO;
import com.campusbite.entity.CartItem;
import com.campusbite.mapper.CartItemMapper;
import com.campusbite.service.CartItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('STUDENT')")
public class CartItemController {

    private final CartItemService cartItemService;
    private final CartItemMapper cartItemMapper;

    public CartItemController(CartItemService cartItemService, CartItemMapper cartItemMapper) {
        this.cartItemService = cartItemService;
        this.cartItemMapper = cartItemMapper;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getCart(Principal principal) {
        List<CartItemResponseDTO> response = cartItemService.getCartItems(principal.getName())
                .stream()
                .map(cartItemMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CartItemResponseDTO> addToCart(
            Principal principal,
            @RequestBody CartItemRequestDTO request) {
        CartItem cartItem = cartItemService.addToCart(principal.getName(), request.getFoodItemId(), request.getQuantity());
        return ResponseEntity.ok(cartItemMapper.toResponseDTO(cartItem));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateQuantity(
            Principal principal,
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        CartItem updatedItem = cartItemService.updateCartItemQuantity(principal.getName(), cartItemId, quantity);
        if (updatedItem == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cartItemMapper.toResponseDTO(updatedItem));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(
            Principal principal,
            @PathVariable Long cartItemId) {
        cartItemService.removeFromCart(principal.getName(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Principal principal) {
        cartItemService.clearCart(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
