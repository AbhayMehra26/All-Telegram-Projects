package com.example.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.UserData;

@Repository
public interface UserRepository extends JpaRepository<UserData, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
	UserData findByUsername(String username);
	UserData findByChatId(Long chatId);
}