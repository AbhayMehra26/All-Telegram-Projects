package com.example.controller;

import com.example.model.Appointment;
import com.example.repository.AppointmentRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Controller
@RequestMapping("date-time") // Corrected path
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final TelegramBot telegramBot;

    // Constructor Injection
    public AppointmentController(AppointmentRepository appointmentRepository, TelegramBot telegramBot) {
        this.appointmentRepository = appointmentRepository;
        this.telegramBot = telegramBot;
    }

    // ✅ Show the Appointment Page
    @GetMapping("/appointment")
    public String showAppointmentPage() {
        return "booking"; // Make sure booking.html is in /src/main/resources/templates or /static
    }

    // ✅ Handle Form Submission & Save Appointment
    @PostMapping("/submit-appointment")
    public String submitAppointment(@RequestParam Map<String, String> params) {
        try {
            Long chatId = Long.parseLong(params.get("chatId"));
            String serviceName = params.get("service");
            LocalDateTime appointmentDateTime = LocalDateTime.parse(params.get("dateTime"));

            // Save appointment in the database
            Appointment appointment = new Appointment(chatId.toString(), serviceName, appointmentDateTime);
            appointmentRepository.save(appointment);

            // ✅ Send Confirmation Message to Telegram User
            String message = "📅 Your appointment for *" + serviceName + "* is booked on *" + appointmentDateTime + "*.";
            telegramBot.sendTextMessage(chatId, message);  

            return "confirmation"; // Return a confirmation page

        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            return "error"; // Return an error page if parsing fails
        }
    }
}
