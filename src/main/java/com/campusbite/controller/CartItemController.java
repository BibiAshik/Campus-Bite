package com.campusbite.controller;

import com.campusbite.dto.request.CartItemRequestDTO;
import com.campusbite.dto.response.CartItemResponseDTO;
import com.campusbite.service.CartItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('STUDENT')")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getCart(Principal principal) {
        return ResponseEntity.ok(cartItemService.getCartItems(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<CartItemResponseDTO> addToCart(
            Principal principal,
            @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartItemService.addToCart(principal.getName(), request));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateQuantity(
            Principal principal,
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        CartItemResponseDTO updatedItem = cartItemService.updateCartItemQuantity(principal.getName(), cartItemId, quantity);
        if (updatedItem == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(updatedItem);
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
