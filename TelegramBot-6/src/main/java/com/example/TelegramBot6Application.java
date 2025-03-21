package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.BotService.TelegramBotService;

@SpringBootApplication
public class TelegramBot6Application {

	public static void main(String[] args) {
		// Start Spring Application and get the application context 
		//SpringApplication.run(...) starts the Spring Boot application.
        ApplicationContext context = SpringApplication.run(TelegramBot6Application.class, args);

        // Get the bot bean from Spring's Application Context
        //This means TelegramBotService is managed by Spring (probably annotated with @Component or @Service).
       //It initializes the Spring environment, scans for components, and sets up the Spring IoC container (ApplicationContext).
        TelegramBotService botService = context.getBean(TelegramBotService.class);

        // Create an object of TelegramBotsApi with DefaultBotSession.class.
      //  Register the bot with Telegram using botsApi.registerBot(botService).
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(botService);  // Register the bot correctly
            System.out.println("✅ Bot started successfully!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }	
     }	
}


