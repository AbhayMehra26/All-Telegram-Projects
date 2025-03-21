package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.BotService.TelegramBotService;

@SpringBootApplication
public class TelegramBot5Application {

	public static void main(String[] args) {
		// Start Spring Application and get the application context
        ApplicationContext context = SpringApplication.run(TelegramBot5Application.class, args);

        // Get the bot bean from Spring's Application Context
        TelegramBotService botService = context.getBean(TelegramBotService.class);

        // Initialize and register the bot
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(botService);  // Register the bot correctly
            System.out.println("✅ Bot started successfully!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }	}

}
