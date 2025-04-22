package com.example.repository;

import com.example.model.UserData;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByUsername(String username);
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
}