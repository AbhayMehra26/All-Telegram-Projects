package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.UserData;

public interface UserRepository extends JpaRepository<UserData, Long>
	{
	}


