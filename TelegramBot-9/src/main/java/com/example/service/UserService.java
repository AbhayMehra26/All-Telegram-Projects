package com.example.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.example.model.UserData;
import com.example.repository.UserRepository;

@Service
@Validated  // Enables validation at the service layer
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String registerUser(@Valid UserData userData) {
        userRepository.save(userData);  // Validation occurs before saving
        return "✅ User registered successfully!";
    }
}