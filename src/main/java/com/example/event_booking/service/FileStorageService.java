package com.example.event_booking.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    // store files in project-root/uploads/events
    private final Path root;

    public FileStorageService() {
        this.root = Paths.get("uploads", "events").toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            System.out.println("FileStorageService: uploads root = " + root.toString());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage (create directories): " + root, e);
        }
    }

    /**
     * Store file and return a web-accessible relative path like "/uploads/events/1234-name.png".
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // optional: check size here too (defensive)
        long maxBytes = 20L * 1024 * 1024; // 20 MB
        if (file.getSize() > maxBytes) {
            throw new RuntimeException("File too large (max 20 MB)");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String filename = System.currentTimeMillis() + "-" + original.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

        try {
            Path target = root.resolve(filename).normalize();
            // Prevent path traversal
            if (!target.getParent().equals(root)) {
                throw new RuntimeException("Cannot store file outside uploads directory.");
            }
            // transfer file
            file.transferTo(target.toFile());
            // return path used by client to GET the file (WebConfig maps /uploads/** to file:uploads/)
            return "/uploads/events/" + filename;
        }
        catch (IOException e) {
            // log then rethrow so controller can return a sensible response
            System.err.println("FileStorageService: Failed to store file " + filename + " -> " + e.getMessage());
            throw new RuntimeException("Could not store file " + filename, e);
        }
    }
}