package com.example.event_booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Keep explicit hosts for fixed backend origins (LAN / container host / emulator)
                .allowedOrigins(
                        "http://192.168.1.7:8083", // your LAN IP backend (if frontend uses this)
                        "http://10.0.2.2:8083",    // Android emulator host mapping (if needed)
                        "http://127.0.0.1:8083"    // backend itself when called from same origin
                )
                // Allow localhost/127.0.0.1 on ANY port (useful for Flutter web dev server which has a random port)
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                // HTTP methods you use from the frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // Allow any request header from the client
                .allowedHeaders("*")
                // Expose headers the client might need (important for Authorization responses etc.)
                .exposedHeaders("Authorization", "Content-Type", "Location")
                // Allow credentials (cookies / Authorization header). OK because we used explicit origins / patterns.
                .allowCredentials(true)
                .maxAge(3600);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}