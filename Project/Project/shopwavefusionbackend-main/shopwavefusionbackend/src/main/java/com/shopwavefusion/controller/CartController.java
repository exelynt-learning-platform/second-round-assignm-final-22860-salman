package com.shopwavefusion.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopwavefusion.exception.CartItemException;
import com.shopwavefusion.exception.ProductException;
import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.request.AddItemRequest;
import com.shopwavefusion.response.ApiResponse;
import com.shopwavefusion.service.CartItemService;
import com.shopwavefusion.service.CartService;
import com.shopwavefusion.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	private CartService cartService;
	private UserService userService;
	private CartItemService cartItemService;
	
	public CartController(CartService cartService, UserService userService, CartItemService cartItemService) {
		this.cartService = cartService;
		this.userService = userService;
		this.cartItemService = cartItemService;
	}
	
	@GetMapping("/")
	public ResponseEntity<?> findUserCartHandler(@RequestHeader("Authorization") String jwt) throws UserException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			Cart cart = cartService.findUserCart(user.getId());
			return new ResponseEntity<>(cart, HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		}
	}
	
	@PostMapping("/add")
	public ResponseEntity<?> addItemToCart(@Valid @RequestBody AddItemRequest req, @RequestHeader("Authorization") String jwt) throws UserException, ProductException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			if (req.getProductId() == null || req.getProductId() <= 0) {
				return new ResponseEntity<>(new ApiResponse("Product ID is required and must be positive", false), HttpStatus.BAD_REQUEST);
			}
			if (req.getQuantity() == null || req.getQuantity() <= 0) {
				return new ResponseEntity<>(new ApiResponse("Quantity must be greater than 0", false), HttpStatus.BAD_REQUEST);
			}
			if (req.getSize() == null || req.getSize().isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Size is required", false), HttpStatus.BAD_REQUEST);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			CartItem createdCartItem = cartService.addCartItem(user.getId(), req);
			return new ResponseEntity<>(createdCartItem, HttpStatus.CREATED);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		} catch (ProductException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.NOT_FOUND);
		}
	}
	
	@DeleteMapping("/remove/{cartItemId}")
	public ResponseEntity<?> removeItemFromCart(@PathVariable Long cartItemId, @RequestHeader("Authorization") String jwt) throws UserException, CartItemException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			if (cartItemId == null || cartItemId <= 0) {
				return new ResponseEntity<>(new ApiResponse("Cart item ID is required and must be positive", false), HttpStatus.BAD_REQUEST);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			cartItemService.removeCartItem(cartItemId, user.getId());
			return new ResponseEntity<>(new ApiResponse("Item removed from cart successfully", true), HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		} catch (CartItemException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping("/update/{cartItemId}")
	public ResponseEntity<?> updateCartItem(@PathVariable Long cartItemId, @Valid @RequestBody AddItemRequest req, @RequestHeader("Authorization") String jwt) throws UserException, CartItemException, ProductException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			if (cartItemId == null || cartItemId <= 0) {
				return new ResponseEntity<>(new ApiResponse("Cart item ID is required and must be positive", false), HttpStatus.BAD_REQUEST);
			}
			if (req.getQuantity() == null || req.getQuantity() <= 0) {
				return new ResponseEntity<>(new ApiResponse("Quantity must be greater than 0", false), HttpStatus.BAD_REQUEST);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			CartItem updatedCartItem = cartItemService.updateCartItem(cartItemId, req, user.getId());
			return new ResponseEntity<>(updatedCartItem, HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		} catch (CartItemException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.NOT_FOUND);
		} catch (ProductException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping("/clear")
	public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String jwt) throws UserException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			cartService.clearCart(user.getId());
			return new ResponseEntity<>(new ApiResponse("Cart cleared successfully", true), HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		}
	}
}
