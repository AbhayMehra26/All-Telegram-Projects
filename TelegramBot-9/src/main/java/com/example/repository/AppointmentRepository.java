package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
