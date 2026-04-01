package com.luv2code.ecommerce.rest;

import com.luv2code.ecommerce.entity.Cart;
import com.luv2code.ecommerce.entity.CartItem;
import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.service.CartService;
import com.luv2code.ecommerce.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("isAuthenticated()")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    /**
     * Get current user's cart
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Cart cart = cartService.viewCart(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("cartId", cart.getId());
            response.put("totalPrice", cart.getTotalPrice());
            response.put("totalQuantity", cart.getTotalQuantity());
            response.put("cartItems", convertCartItemsToMap(cart.getCartItems()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to get cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * Add product to cart
     * POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addProductToCart(@RequestBody Map<String, Object> request) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Long productId = Long.parseLong(String.valueOf(request.get("productId")));
            int quantity = Integer.parseInt(String.valueOf(request.getOrDefault("quantity", 1)));

            if (quantity <= 0) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Quantity must be greater than 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
            }

            CartItem cartItem = cartService.addProductToCart(user.getId(), productId, quantity);
            Cart cart = cartService.viewCart(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added to cart successfully");
            response.put("cartId", cart.getId());
            response.put("cartItemId", cartItem.getId());
            response.put("totalPrice", cart.getTotalPrice());
            response.put("totalQuantity", cart.getTotalQuantity());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to add product to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/update/{cartItemId}
     */
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Integer quantity = request.get("quantity");
            if (quantity == null || quantity <= 0) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Invalid quantity");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
            }

            CartItem updatedItem = cartService.updateCartItemQuantity(user.getId(), cartItemId, quantity);
            Cart cart = cartService.viewCart(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cart item updated successfully");
            response.put("cartItemId", updatedItem.getId());
            response.put("quantity", updatedItem.getQuantity());
            response.put("totalPrice", cart.getTotalPrice());
            response.put("totalQuantity", cart.getTotalQuantity());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to update cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * Remove product from cart
     * DELETE /api/cart/remove/{cartItemId}
     */
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeProductFromCart(@PathVariable Long cartItemId) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            cartService.removeProductFromCart(user.getId(), cartItemId);
            Cart cart = cartService.viewCart(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product removed from cart");
            response.put("totalPrice", cart.getTotalPrice());
            response.put("totalQuantity", cart.getTotalQuantity());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to remove product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * Clear entire cart
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            cartService.clearCart(user.getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cart cleared successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to clear cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    private List<Map<String, Object>> convertCartItemsToMap(Set<CartItem> cartItems) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("cartItemId", item.getId());
                itemMap.put("productId", item.getProduct().getId());
                itemMap.put("productName", item.getProduct().getName());
                itemMap.put("productImage", item.getProduct().getImageUrl());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("totalPrice", item.getUnitPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
                items.add(itemMap);
            }
        }
        return items;
    }
}
