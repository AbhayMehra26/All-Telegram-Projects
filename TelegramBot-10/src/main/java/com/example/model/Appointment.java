package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor  // Required by JPA
@AllArgsConstructor // Generates a constructor with all fields
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private String serviceName;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    // Custom constructor
    public Appointment(Long chatId, String serviceName, LocalDateTime dateTime) {
        this.chatId = chatId;
        this.serviceName = serviceName;
        this.dateTime = dateTime;
    }
}
