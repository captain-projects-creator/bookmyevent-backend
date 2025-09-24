package com.example.event_booking.security;

import com.example.event_booking.service.UserService;
import com.example.event_booking.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JwtAuthenticationFilter
 *
 * - short-circuits static files and OPTIONS requests
 * - marks UserService @Lazy to help avoid circular dependency problems
 * - safely validates token and sets SecurityContext once
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserService userService;

    // mark UserService lazy to help break circular deps if any
    public JwtAuthenticationFilter(JwtUtil jwtUtil, @Lazy UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // JwtAuthenticationFilter (keep your class, just ensure this short-circuit is present)
        String path = req.getRequestURI();

// Skip static assets, auth endpoints and options preflight quickly
        if (path.startsWith("/static/") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".map") ||
                path.endsWith(".ico") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                "OPTIONS".equalsIgnoreCase(req.getMethod()) ||
                "/".equals(path) ||
                "/index.html".equals(path) ||
                path.startsWith("/api/auth/")) {   // <<-- IMPORTANT: skip auth endpoints
            chain.doFilter(req, res);
            return;
        }

        // If already authenticated (e.g. another filter), skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        String auth = req.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            // no token - proceed as anonymous
            chain.doFilter(req, res);
            return;
        }

        String token = auth.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            chain.doFilter(req, res);
            return;
        }

        try {
            // validate token and obtain username
            String username = jwtUtil.validateAndGetUsername(token);
            if (username != null) {
                // Try to obtain user (this may hit DB). We catch and continue if it fails.
                try {
                    var user = userService.findByUsername(username);

                    // Build authorities. If you store role in user.getRole(), use it; otherwise provide a default.
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (user != null && user.getRole() != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                    } else {
                        // default to a generic authenticated role (useful if JWT lacks role claim)
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }

                    // principal: you can put user object (not recommended for large entities) or username
                    var authToken = new UsernamePasswordAuthenticationToken(
                            username, // principal (keeps SecurityContext lightweight)
                            null,
                            authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } catch (Exception e) {
                    // If user lookup fails, we still treat token invalid for authorization purposes.
                    log.warn("Failed to load user '{}' during JWT processing: {}", username, e.getMessage());
                }
            }
        } catch (Exception ex) {
            // token validation failed (expired / malformed / signature) — log and continue without authentication
            log.debug("Invalid/expired JWT: {}", ex.getMessage());
        }

        chain.doFilter(req, res);
    }
}