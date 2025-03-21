package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.UserData;

public interface UserRepository extends JpaRepository<UserData, Long>
	{
    Optional<UserData> findByUsernameAndPassword(String username, String password);

	}


