package com.example.event_booking.controller;

import com.example.event_booking.exception.ResourceNotFoundException;
import com.example.event_booking.model.Event;
import com.example.event_booking.service.EventService;
import com.example.event_booking.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;
    private final FileStorageService fileStorageService;

    // Constructor injection for services
    public EventController(EventService eventService, FileStorageService fileStorageService) {
        this.eventService = eventService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/events")
    public List<Event> listEvents() {
        return eventService.listEvents();
    }

    /**
     * Create new event (admin only) - JSON body.
     * Expects JSON body matching Event (title, description, date, capacity).
     * Returns 201 Created with Location header.
     */
    @PostMapping(value = "/events", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            Event created = eventService.createEvent(event);
            // return 201 with Location header
            return ResponseEntity.created(URI.create("/api/events/" + created.getId()))
                    .body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
        }
    }

    /**
     * Create new event (admin only) - multipart form with optional image.
     * Form fields: title, description, date (YYYY-MM-DD), capacity, image (file, optional).
     */
    @PostMapping(value = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEventMultipart(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("date") String date,
            @RequestParam("capacity") Integer capacity,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setDate(LocalDate.parse(date));
            event.setCapacity(capacity);

            if (image != null && !image.isEmpty()) {
                String imagePath = fileStorageService.store(image);
                event.setImagePath(imagePath);
            }

            Event created = eventService.createEvent(event);
            return ResponseEntity.created(URI.create("/api/events/" + created.getId())).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete event and any bookings that reference it.
     * Admin-only.
     */
    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
        }
    }
}