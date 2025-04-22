package com.example.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import com.example.model.Appointment;
import com.example.repository.AppointmentRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public void saveAppointment(Long chatId, String serviceName, String dateTime) {
        // Parse the date-time string into LocalDateTime
        LocalDateTime appointmentTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Create a new Appointment object
        Appointment appointment = new Appointment(chatId, serviceName, appointmentTime);

        // Save to the database
        appointmentRepository.save(appointment);
    }
}
