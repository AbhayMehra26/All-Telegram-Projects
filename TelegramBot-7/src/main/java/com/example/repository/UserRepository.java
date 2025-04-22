package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.UserData;

@Repository
public interface UserRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByUsername(String username);
}


