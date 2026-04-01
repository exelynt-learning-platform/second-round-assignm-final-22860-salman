package com.shopwavefusion.controller;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.UserRepository;
import com.shopwavefusion.request.LoginRequest;
import com.shopwavefusion.response.ApiResponse;
import com.shopwavefusion.response.AuthResponse;
import com.shopwavefusion.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private CartService cartService;
	private AuthenticationManager authenticationManager;
	
	public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, CartService cartService,
			AuthenticationManager authenticationManager) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.cartService = cartService;
		this.authenticationManager = authenticationManager;
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> createUserHandler(@Valid @RequestBody User user) throws UserException{
		if (user.getEmail() == null || user.getEmail().isEmpty()) {
			return new ResponseEntity<>(new ApiResponse("Email is required", false), HttpStatus.BAD_REQUEST);
		}
		if (user.getPassword() == null || user.getPassword().length() < 6) {
			return new ResponseEntity<>(new ApiResponse("Password must be at least 6 characters", false), HttpStatus.BAD_REQUEST);
		}
		
        String email = user.getEmail();
        String password = user.getPassword();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        
        User isEmailExist = userRepository.findByEmail(email);
        if (isEmailExist != null) {
            return new ResponseEntity<>(new ApiResponse("Email is already in use", false), HttpStatus.BAD_REQUEST);
        }

        User createdUser = new User();
        createdUser.setEmail(email);
        createdUser.setFirstName(firstName);
        createdUser.setLastName(lastName);
        createdUser.setPassword(passwordEncoder.encode(password));
        createdUser.setRole("ROLE_USER");
        createdUser.setCreatedAt(LocalDateTime.now());
        createdUser.setMobile(user.getMobile());
        
        User savedUser = userRepository.save(createdUser);
        cartService.createCart(savedUser);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        return new ResponseEntity<>(new ApiResponse("User created successfully", true), HttpStatus.CREATED);
	}
	
	@PostMapping("/signin")
	public ResponseEntity<?> signin(@Valid @RequestBody LoginRequest loginRequest) throws UserException {
		if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
			return new ResponseEntity<>(new ApiResponse("Email is required", false), HttpStatus.BAD_REQUEST);
		}
		if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
			return new ResponseEntity<>(new ApiResponse("Password is required", false), HttpStatus.BAD_REQUEST);
		}
		
		User user = userRepository.findByEmail(loginRequest.getEmail());
		if (user == null) {
			return new ResponseEntity<>(new ApiResponse("Invalid email or password", false), HttpStatus.UNAUTHORIZED);
		}
		
		if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
			return new ResponseEntity<>(new ApiResponse("Invalid email or password", false), HttpStatus.UNAUTHORIZED);
		}
		
		try {
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginRequest.getEmail(),
					loginRequest.getPassword()
				)
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return new ResponseEntity<>(user, HttpStatus.OK);
		} catch (BadCredentialsException e) {
			return new ResponseEntity<>(new ApiResponse("Invalid email or password", false), HttpStatus.UNAUTHORIZED);
		}
	}
