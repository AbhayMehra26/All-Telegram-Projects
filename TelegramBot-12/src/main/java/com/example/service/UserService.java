package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.model.UserData;
import com.example.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String registerUser(String username, String password, String email, String phone) {
        if (userRepository.existsByUsername(username)) return "Username already taken!";
        if (userRepository.existsByEmail(email)) return "Email already used!";
        if (userRepository.existsByPhone(phone)) return "Phone number already exists!";

        // Encrypt password before saving
        UserData user = new UserData(username, passwordEncoder.encode(password), email, phone);
        userRepository.save(user);
        
        return "✅ Account created successfully!";
    }
}
