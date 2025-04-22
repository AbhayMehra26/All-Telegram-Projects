package com.example.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.controller.TelegramBot;



@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot botService) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        
        // Ensure the bot is only using long polling, not webhook mode
        try {
            botsApi.registerBot(botService);
            System.out.println("✅ Bot started successfully!");

        } 
        catch (TelegramApiException e) 
        {
            e.printStackTrace();
        }
        
        return botsApi;
    }
}
