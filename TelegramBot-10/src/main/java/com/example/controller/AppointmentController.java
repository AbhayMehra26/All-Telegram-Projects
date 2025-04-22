package com.example.controller;

import com.example.model.Appointment;
import com.example.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Show the date-time picker form
    @GetMapping("/book")
    public String showBookingForm(@RequestParam Long chatId, @RequestParam String serviceName, Model model) {
        model.addAttribute("chatId", chatId);
        model.addAttribute("serviceName", serviceName);
        return "appointment";  // Refers to appointment.html
    }

    // Handle form submission
    @PostMapping("/save")
    public String saveAppointment(@RequestParam Long chatId, 
                                  @RequestParam String serviceName, 
                                  @RequestParam String datetime, 
                                  Model model) {
        // Convert String to LocalDateTime
        LocalDateTime appointmentTime = LocalDateTime.parse(datetime);

        // Save to database
        Appointment appointment = new Appointment(chatId, serviceName, appointmentTime);
        appointmentRepository.save(appointment);

        // Confirmation message
        model.addAttribute("message", "✅ Your appointment for " + serviceName + " is booked on " + datetime);
        return "confirmation";  // Redirect to confirmation page
    }
}
