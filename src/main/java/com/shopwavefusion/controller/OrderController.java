package com.shopwavefusion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopwavefusion.exception.OrderException;
import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Order;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.request.CreateOrderRequest;
import com.shopwavefusion.request.StripePaymentRequest;
import com.shopwavefusion.response.ApiResponse;
import com.shopwavefusion.response.StripePaymentResponse;
import com.shopwavefusion.service.OrderService;
import com.shopwavefusion.service.StripePaymentService;
import com.shopwavefusion.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {
	
	private OrderService orderService;
	private UserService userService;
	private StripePaymentService stripePaymentService;
	
	public OrderController(OrderService orderService, UserService userService, StripePaymentService stripePaymentService) {
		this.orderService = orderService;
		this.userService = userService;
		this.stripePaymentService = stripePaymentService;
	}
	
	@PostMapping("/")
	public ResponseEntity<?> createOrderHandler(@Valid @RequestBody CreateOrderRequest orderRequest,
			@RequestHeader("Authorization") String jwt) throws UserException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			if (orderRequest.getStreetAddress() == null || orderRequest.getStreetAddress().isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Street address is required", false), HttpStatus.BAD_REQUEST);
			}
			if (orderRequest.getCity() == null || orderRequest.getCity().isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("City is required", false), HttpStatus.BAD_REQUEST);
			}
			if (orderRequest.getState() == null || orderRequest.getState().isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("State is required", false), HttpStatus.BAD_REQUEST);
			}
			if (orderRequest.getZipCode() == null || orderRequest.getZipCode().isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Zip code is required", false), HttpStatus.BAD_REQUEST);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			Order order = orderService.createOrder(user, orderRequest);
			return new ResponseEntity<>(order, HttpStatus.CREATED);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ApiResponse("Error creating order: " + e.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/user")
	public ResponseEntity<?> usersOrderHistoryHandler(@RequestHeader("Authorization") String jwt) throws OrderException, UserException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			List<Order> orders = orderService.usersOrderHistory(user.getId());
			return new ResponseEntity<>(orders, HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		} catch (OrderException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/{orderId}")
	public ResponseEntity<?> findOrderHandler(@PathVariable Long orderId, @RequestHeader("Authorization") String jwt) throws OrderException, UserException {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			if (orderId == null || orderId <= 0) {
				return new ResponseEntity<>(new ApiResponse("Order ID is required and must be positive", false), HttpStatus.BAD_REQUEST);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			Order order = orderService.findOrderById(orderId);
			if (!order.getUser().getId().equals(user.getId())) {
				return new ResponseEntity<>(new ApiResponse("You can't view another user's order", false), HttpStatus.FORBIDDEN);
			}
			
			return new ResponseEntity<>(order, HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		} catch (OrderException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping("/payment/stripe")
	public ResponseEntity<?> createOrderWithStripePaymentHandler(@Valid @RequestBody StripePaymentRequest paymentRequest,
			@RequestHeader("Authorization") String jwt) throws UserException, Exception {
		try {
			if (jwt == null || jwt.isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Authorization header is required", false), HttpStatus.UNAUTHORIZED);
			}
			if (paymentRequest.getAmount() == null || paymentRequest.getAmount() <= 0) {
				return new ResponseEntity<>(new ApiResponse("Amount is required and must be greater than 0", false), HttpStatus.BAD_REQUEST);
			}
			if (paymentRequest.getToken() == null || paymentRequest.getToken().isEmpty()) {
				return new ResponseEntity<>(new ApiResponse("Payment token is required", false), HttpStatus.BAD_REQUEST);
			}
			
			User user = userService.findUserProfileByJwt(jwt);
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse("User not found", false), HttpStatus.NOT_FOUND);
			}
			
			// Process Stripe payment
			StripePaymentResponse paymentResponse = stripePaymentService.processPayment(paymentRequest);
			
			if (!paymentResponse.isSuccess()) {
				return new ResponseEntity<>(new ApiResponse("Payment failed: " + paymentResponse.getMessage(), false), HttpStatus.PAYMENT_REQUIRED);
			}
			
			// Create order with Stripe payment
			Order order = orderService.createOrderWithStripePayment(user, paymentRequest, paymentResponse);
			return new ResponseEntity<>(order, HttpStatus.CREATED);
		} catch (UserException e) {
			return new ResponseEntity<>(new ApiResponse(e.getMessage(), false), HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ApiResponse("Error processing payment: " + e.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
