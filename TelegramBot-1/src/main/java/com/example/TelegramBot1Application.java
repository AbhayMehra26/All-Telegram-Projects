package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.BotService.TelegramBotService;

@SpringBootApplication
public class TelegramBot1Application {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBot1Application.class, args);

	
		// Initialize the bot
        try {
            // Using the updated constructor for the latest TelegramBotsApi
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBotService());  // Register the bot to receive messages
            System.out.println("Bot start Successfully");
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
	
	}

	
}
