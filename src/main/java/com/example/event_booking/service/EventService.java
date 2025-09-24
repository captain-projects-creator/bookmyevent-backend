package com.example.event_booking.service;

import com.example.event_booking.exception.ResourceNotFoundException;
import com.example.event_booking.model.Event;
import com.example.event_booking.repository.BookingRepository;
import com.example.event_booking.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class EventService {
    private final EventRepository repo;
    private final BookingRepository bookingRepo;

    public EventService(EventRepository repo, BookingRepository bookingRepo) {
        this.repo = repo;
        this.bookingRepo = bookingRepo;
    }

    public List<Event> listEvents() { return repo.findAll(); }

    /**
     * Create and persist a new Event.
     * Validates required fields and throws IllegalArgumentException on bad input.
     */
    public Event createEvent(Event event) {
        if (event == null) throw new IllegalArgumentException("event body required");
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) throw new IllegalArgumentException("title required");
        if (event.getDate() == null) throw new IllegalArgumentException("date required");
        if (event.getCapacity() == null || event.getCapacity() < 1) throw new IllegalArgumentException("capacity must be >= 1");

        Event e = new Event();
        e.setTitle(event.getTitle().trim());
        e.setDescription(event.getDescription());
        e.setDate(event.getDate());
        e.setCapacity(event.getCapacity());
        return repo.save(e);
    }

    /**
     * Delete an event and any bookings that reference it.
     * This runs inside a transaction to ensure consistency.
     *
     * @param eventId id of the event to delete
     * @throws ResourceNotFoundException if event doesn't exist
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        // verify event exists; throw 404 otherwise
        Event ev = repo.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        // delete bookings for this event first
        bookingRepo.deleteByEventId(eventId);

        // then delete event record
        repo.deleteById(eventId);
    }
}
