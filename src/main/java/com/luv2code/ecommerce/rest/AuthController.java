package com.luv2code.ecommerce.rest;

import com.luv2code.ecommerce.dto.LoginRequest;
import com.luv2code.ecommerce.dto.RegisterRequest;
import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req, BindingResult binding) {
        if (binding.hasErrors()) {
            Map<String, String> errs = new HashMap<>();
            binding.getFieldErrors().forEach(e -> 
                errs.put(e.getField(), e.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errs);
        }

        try {
            User user = authService.registerUser(
                    req.getUsername(),
                    req.getEmail(),
                    req.getPassword(),
                    req.getFirstName(),
                    req.getLastName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            String token = authService.loginUser(req.getUsername(), req.getPassword());
            Map<String, String> res = new HashMap<>();
            res.put("token", token);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        try {
            User u = authService.getCurrentUser();
            if (u == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }
            return ResponseEntity.ok(u);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Map<String, String> data) {
        try {
            User current = authService.getCurrentUser();
            if (current == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            User updated = authService.updateUser(
                    current.getId(),
                    data.get("firstName"),
                    data.get("lastName"),
                    data.get("phoneNumber"),
                    data.get("address"),
                    data.get("city"),
                    data.get("state"),
                    data.get("zipCode"),
                    data.get("country")
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}
