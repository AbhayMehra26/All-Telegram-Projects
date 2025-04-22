package com.example.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/book-appointment")
public class AppointmentController {

    @Autowired
    private TelegramBotService telegramBotService;
    
    @GetMapping("picker")
    public String showDatePicker(@RequestParam Long chatId, @RequestParam String service, Model model) {
        model.addAttribute("chatId", chatId);
        model.addAttribute("service", service);
        return "book-appointment"; // Thymeleaf template
    }

    @PostMapping
    public String confirmAppointment(@RequestParam Long chatId, @RequestParam String service, @RequestParam String dateTime) {
        telegramBotService.confirmAppointment(chatId, service, dateTime);
        return "confirmation";
    }
}
