package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF if not needed
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)) // Allow session-based authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/date-time/appointment", "/static/**", "/login").permitAll() // Allow public access
                .anyRequest().authenticated() // Require authentication for all other routes
            )
            .formLogin(form -> form
                .loginPage("/login") // Custom login page
                .permitAll() // Ensure login page is accessible
            )
            .logout(logout -> logout.logoutSuccessUrl("/")); // Redirect to home after logout

        return http.build();
    }
}
