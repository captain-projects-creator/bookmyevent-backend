package com.example.event_booking.service;

import com.example.event_booking.exception.ResourceNotFoundException;
import com.example.event_booking.model.Booking;
import com.example.event_booking.model.Event;
import com.example.event_booking.model.User;
import com.example.event_booking.repository.BookingRepository;
import com.example.event_booking.repository.EventRepository;
import com.example.event_booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final EventRepository eventRepo;
    private final UserRepository userRepo;

    @Autowired
    public BookingService(BookingRepository bookingRepo, EventRepository eventRepo, UserRepository userRepo) {
        this.bookingRepo = bookingRepo;
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    public Booking bookTicket(Long userId, Long eventId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);

        return bookingRepo.save(booking);
    }

    public void cancelBooking(Long bookingId) {
        bookingRepo.deleteById(bookingId);
    }

    public List<Booking> getBookings() {
        return bookingRepo.findAll();
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepo.findByUserId(userId);
    }

    public Booking getBookingById(Long id) {
        return bookingRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    /**
     * Helper to find user by username for controllers that need the user object.
     * Uses a RuntimeException message (avoids passing String into a constructor that expects Long).
     */
    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}