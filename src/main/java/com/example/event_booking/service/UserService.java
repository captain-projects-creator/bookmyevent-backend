package com.example.event_booking.service;

import com.example.event_booking.model.User;
import com.example.event_booking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        if (user.getUsername() == null) throw new IllegalArgumentException("username required");
        String username = user.getUsername().trim();
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("username already taken");
        }
        if (user.getEmail() != null && user.getEmail().trim().length() > 0) {
            if (userRepo.existsByEmail(user.getEmail().trim())) {
                throw new IllegalArgumentException("email already taken");
            }
        }
        if (user.getMobile() != null && user.getMobile().trim().length() > 0) {
            if (userRepo.existsByMobile(user.getMobile().trim())) {
                throw new IllegalArgumentException("mobile already taken");
            }
        }

        String raw = user.getPassword() == null ? "" : user.getPassword();
        user.setPassword(passwordEncoder.encode(raw));

        if (user.getRole() == null) user.setRole("USER");
        user.setUsername(username);
        if (user.getEmail() != null) user.setEmail(user.getEmail().trim());
        if (user.getMobile() != null) user.setMobile(user.getMobile().trim());
        return userRepo.save(user);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public User findByEmailOrMobile(String dest) {
        if (dest == null) return null;
        if (dest.contains("@")) {
            return userRepo.findByEmail(dest).orElse(null);
        } else {
            return userRepo.findByMobile(dest).orElse(null);
        }
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null) rawPassword = "";
        if (encodedPassword == null) return false;
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}