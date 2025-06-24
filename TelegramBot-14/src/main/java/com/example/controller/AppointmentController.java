package com.example.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.model.Appointment;
import com.example.repository.AppointmentRepository;

@Controller
public class AppointmentController {

	@Autowired
    private AppointmentRepository appointmentRepository;  // ✅ Injected

	
    @GetMapping("/appointment")
    public String showAppointmentPage(@RequestParam Long chatId, @RequestParam String service, Model model) {
        model.addAttribute("chatId", chatId);
        model.addAttribute("service", service);
        return "index";  // will render appointment.html
    }

    @PostMapping("/api/appointments")
    @ResponseBody
    public String saveAppointment(
            @RequestParam Long chatId,
            @RequestParam String serviceName,
            @RequestParam String datetime) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime appointmentDateTime = LocalDateTime.parse(datetime, formatter);

            Appointment appointment = new Appointment();
            appointment.setChatId(chatId);
            appointment.setServiceName(serviceName);
            appointment.setAppointmentDateTime(appointmentDateTime);
            appointment.setUsername("N/A"); // Optional: Set real username if available

            appointmentRepository.save(appointment);
            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to save appointment";
        }
        
    }
   
}
