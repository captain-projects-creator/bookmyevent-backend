package com.example.event_booking.repository;

import com.example.event_booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    boolean existsByMobile(String mobile);
    Optional<User> findByMobile(String mobile);
}