package com.example.event_booking.controller;

import com.example.event_booking.model.Booking;
import com.example.event_booking.model.User;
import com.example.event_booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping("/book/{userId}/{eventId}")
    public Booking bookTicket(@PathVariable Long userId, @PathVariable Long eventId) throws Exception {
        return bookingService.bookTicket(userId, eventId);
    }

    // New: book for the currently authenticated user
    @PostMapping("/book/{eventId}")
    public ResponseEntity<?> bookForCurrentUser(org.springframework.security.core.Authentication authentication,
                                                @PathVariable Long eventId) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Unauthorized"));
        }
        String username = authentication.getName();
        User user = bookingService.getUserByUsername(username);
        try {
            Booking booking = bookingService.bookTicket(user.getId(), eventId);
            return ResponseEntity.ok(booking);
        } catch (Exception ex) {
            // If event not found or other business error, propagate message
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public void cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
    }

    @GetMapping
    public List<Booking> getBookings() {
        return bookingService.getBookings();
    }

    // get bookings for specific user
    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsForUser(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    // returns bookings for the authenticated user
    @GetMapping("/me")
    public ResponseEntity<List<Booking>> getMyBookings(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        User user = bookingService.getUserByUsername(username);
        var bookings = bookingService.getBookingsByUser(user.getId());
        return ResponseEntity.ok(bookings);
    }
}