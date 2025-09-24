package com.example.event_booking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Do not create sessions; use stateless JWT auth
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Add JWT filter before username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Permit static resources at common locations (css, js, images, favicon, etc.)
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // Also allow these resource names (if you serve them at project root)
                        .requestMatchers(
                                new AntPathRequestMatcher("/index.html"),
                                new AntPathRequestMatcher("/favicon.ico"),
                                new AntPathRequestMatcher("/app.js"),
                                new AntPathRequestMatcher("/admin.js"),
                                new AntPathRequestMatcher("/admin.html"),
                                new AntPathRequestMatcher("/my_tickets.js"),
                                new AntPathRequestMatcher("/styles.css"),
                                new AntPathRequestMatcher("/static/**")
                        ).permitAll()
                        // Public auth endpoints for login/register
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public GET events
                        .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**").permitAll()
                        // Only ADMIN can create or delete events
                        .requestMatchers(HttpMethod.POST, "/api/events", "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        // Admin endpoints (require ADMIN role)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // All other API under /api require authentication by default
                        .requestMatchers("/api/**").authenticated()
                        // everything else is allowed (static pages)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}