package com.example.event_booking.controller;

import com.example.event_booking.model.User;
import com.example.event_booking.service.UserService;
import com.example.event_booking.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        if (body == null) return ResponseEntity.badRequest().body(Map.of("message","body missing"));
        String username = body.get("username");
        String password = body.get("password");
        String role = body.getOrDefault("role", "USER");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message","username and password required"));
        }
        try {
            User u = new User();
            u.setUsername(username.trim());
            u.setPassword(password);
            u.setRole(role);
            User created = userService.createUser(u);
            return ResponseEntity.ok(Map.of("id", created.getId(), "username", created.getUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message","Internal error"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        if (body == null) return ResponseEntity.badRequest().body(Map.of("message","body missing"));
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) return ResponseEntity.badRequest().body(Map.of("message","username and password required"));

        try {
            var user = userService.findByUsername(username);
            if (!userService.checkPassword(password, user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
            }
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", user.getUsername(),
                    "role", user.getRole()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(Map.of("message","Invalid credentials"));
        }
    }
}