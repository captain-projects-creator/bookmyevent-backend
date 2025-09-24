package com.example.event_booking.repository;

import com.example.event_booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Return all bookings for a given user id
    List<Booking> findByUserId(Long userId);

    // Find bookings by event id (helpful if you want to inspect before deletion)
    List<Booking> findByEventId(Long eventId);

    // Derived delete query: remove all bookings that reference a given event id
    void deleteByEventId(Long eventId);
}