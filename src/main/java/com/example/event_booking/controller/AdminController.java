package com.example.event_booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AdminController - simplified: OTP endpoints removed.
 * Use this controller for admin actions (create/delete events, etc).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // Add admin-level endpoints here (example below is a placeholder)
    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> pingAdmin() {
        return ResponseEntity.ok(Map.of("message", "admin ok"));
    }

    // If you previously had send-otp / verify-otp, they are removed.
    // Add your admin-specific operations (for example: create events via EventController or /api/admin/events if you want)
}