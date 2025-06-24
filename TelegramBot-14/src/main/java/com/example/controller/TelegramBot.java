package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.example.model.UserData;
import com.example.repository.UserRepository;
import com.example.service.UserService;

import java.util.*;
import java.util.regex.*;

@Service
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    private Map<Long, String> userState = new HashMap<>();
    private Map<Long, UserData> tempUserData = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();

            if (text.equalsIgnoreCase("/start")) {
                sendTextMessage(chatId, "Hi dear, please send your name.");
                userState.put(chatId, "AWAITING_NAME");
            } else if (userState.get(chatId) != null && userState.get(chatId).equals("AWAITING_NAME")) {
                sendOptions(chatId);
                userState.remove(chatId);
            } else if (userState.containsKey(chatId)) {
                handleUserInput(chatId, text);
            }
        } else if (update.hasCallbackQuery()) {
        	
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
          
            switch (callbackData) {
            case "create_account":
                tempUserData.put(chatId, new UserData());
                userState.put(chatId, "USERNAME");
                sendTextMessage(chatId, "Please enter your desired username:");
                break;
            case "login":
                userState.put(chatId, "LOGIN_USERNAME");
                sendTextMessage(chatId, "Please enter your username:");
                break;
            case "register_service":
                sendServiceOptions(chatId);
                break;
          
            case "service_AMC":
                sendAppointmentLink(chatId, "AMC Service");
                break;
            case "service_Paid":
                sendAppointmentLink(chatId, "Paid Service");
                break;
            case "service_Free":
                sendAppointmentLink(chatId, "Free Service");
                break;
       }
    }
           
    }

    private void handleUserInput(Long chatId, String text) {
        String state = userState.get(chatId);

        if ("USERNAME".equals(state)) {
            if (!Pattern.matches("^[a-zA-Z0-9_]{3,20}$", text)) {
                sendTextMessage(chatId, "❌ Invalid username! It must be 3-20 characters and can only contain letters, numbers, and underscores.");
                return;
            }
            tempUserData.get(chatId).setUsername(text);
            userState.put(chatId, "PASSWORD");
            sendTextMessage(chatId, "Now enter a strong password:");

        } else if ("PASSWORD".equals(state)) {
            if (!Pattern.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", text)) {
                sendTextMessage(chatId, "❌ Invalid password! It must have at least 6 characters, 1 uppercase, 1 lowercase, 1 digit, and 1 special character.");
                return;
            }
            tempUserData.get(chatId).setPassword(text);
            userState.put(chatId, "EMAIL");
            sendTextMessage(chatId, "Enter your email:");

        } else if ("EMAIL".equals(state)) {
            if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", text)) {
                sendTextMessage(chatId, "❌ Invalid email format! Please enter a valid email.");
                return;
            }
            tempUserData.get(chatId).setEmail(text);
            userState.put(chatId, "PHONE");
            sendTextMessage(chatId, "Enter your phone number:");

        } else if ("PHONE".equals(state)) {
            if (!Pattern.matches("^[0-9]{10}$", text)) {
                sendTextMessage(chatId, "❌ Invalid phone number! It must be exactly 10 digits.");
                return;
            }
            tempUserData.get(chatId).setPhone(text);
            userRepository.save(tempUserData.get(chatId));
            sendTextMessage(chatId, "✅ Account created successfully!");
            tempUserData.remove(chatId);
            userState.remove(chatId);

        } else if ("LOGIN_USERNAME".equals(state)) {
            Optional<UserData> user = userRepository.findByUsername(text);
            if (user.isPresent()) {
                tempUserData.put(chatId, user.get());
                userState.put(chatId, "LOGIN_PASSWORD");
                sendTextMessage(chatId, "Username found! Now, enter your password:");
            } else {
                sendTextMessage(chatId, "❌ Username is wrong, please try again.");
            }

        } else if ("LOGIN_PASSWORD".equals(state)) {
            UserData user = tempUserData.get(chatId);
            if (user.getPassword().equals(text)) {
                sendTextMessage(chatId, "✅ Login successful! Please select an option:");
                showMenu(chatId);
                tempUserData.remove(chatId);
                userState.remove(chatId);
            } else {
                sendTextMessage(chatId, "❌ Invalid password, please try again.");
            }
        }
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please select one of the options:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Create Account").callbackData("create_account").build());
        row.add(InlineKeyboardButton.builder().text("Login").callbackData("login").build());

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Select an option:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("✅ Registration for a service").callbackData("register_service").build());
        row.add(InlineKeyboardButton.builder().text("📜 See my service history and records").callbackData("view_history").build());

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendServiceOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please select a service:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("1️⃣ AMC Service").callbackData("service_AMC").build());
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("2️⃣ Paid Service").callbackData("service_Paid").build());
        keyboard.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder().text("3️⃣ Free Service").callbackData("service_Free").build());
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAppointmentLink(Long chatId, String serviceName) {
  
    	
    	String bookingUrl = "https://dulcet-pika-cb78b7.netlify.app/?chatId=" 
                + chatId + "&serviceName=" + serviceName.replace(" ", "%20");

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setParseMode("HTML");
        message.setText("📅 Click <a href=\"" + bookingUrl + "\">here</a> to book your appointment for " + serviceName + ".");
    

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    

    @Override
    public String getBotUsername() {
        return "teleai23bot"; // Replace with your bot's username
    }

    @Override
    public String getBotToken() {
        return "7387064131:AAECT37CkUpHLW9GwCz27IVN3NQ9AOr_2_A"; // Replace with your actual bot token
    }
    
}