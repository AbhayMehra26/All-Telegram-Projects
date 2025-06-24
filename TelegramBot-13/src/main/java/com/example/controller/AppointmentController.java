package com.example.controller;

import com.example.model.Appointment;
import com.example.repository.AppointmentRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Controller
@RequestMapping("date-time") // Path for appointment
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final TelegramBot telegramBot;

    public AppointmentController(AppointmentRepository appointmentRepository, TelegramBot telegramBot) {
        this.appointmentRepository = appointmentRepository;
        this.telegramBot = telegramBot;
    }

    // ✅ Show Appointment Page (GET request)
    @GetMapping("/appointment")
    public String showAppointmentPage(@RequestParam Long chatId, @RequestParam String serviceName, Model model) {
        // Pass parameters to the HTML page
        model.addAttribute("chatId", chatId);
        model.addAttribute("serviceName", serviceName);
        return "booking"; // This is the booking.html page
    }

    @PostMapping("/submit-appointment")
    @CrossOrigin(origins = "*") // Or specify GitHub origin if needed
    public ResponseEntity<String> submitAppointment(@RequestParam Map<String, String> params) {
        try {
            Long chatId = Long.parseLong(params.get("chatId"));
            String serviceName = params.get("serviceName");
            LocalDateTime appointmentDateTime = LocalDateTime.parse(params.get("datetime"));

            Appointment appointment = new Appointment(chatId.toString(), serviceName, appointmentDateTime);
            appointmentRepository.save(appointment);

            // Notify user on Telegram
            String message = "📅 Your appointment for *" + serviceName + "* is booked on *" + appointmentDateTime + "*.";
            telegramBot.sendTextMessage(chatId, message);

            return ResponseEntity.ok("Appointment booked successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data!");
        }
    }

}
