package com.example.BotService;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.example.model.UserData;
import com.example.model.UserState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final Map<Long, UserState> userStateMap = new HashMap<>();
    private final Map<Long, UserData> userDataMap = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "teleai23bot"; // Replace with your bot's username
    }

    @Override
    public String getBotToken() {
        return "7387064131:AAECT37CkUpHLW9GwCz27IVN3NQ9AOr_2_A"; // Replace with your actual bot token
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleButtonClick(update.getCallbackQuery());
        }
    }
    private void handleTextMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        if (userStateMap.containsKey(chatId)) {
            processUserInput(chatId, text);
        } else if (text.equals("/start")) {
            sendMessage(chatId, "Hi dear, send me your name.");
        } else if (isNameProvided(text)) {
            showOptions(message);
        } else {
            sendMessage(chatId, "Please select an option: Create Account or Login.");
        }
    }
    private void handleButtonClick(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        if ("create_account".equals(callbackData)) {
            userStateMap.put(chatId, UserState.AWAITING_USERNAME);
            userDataMap.put(chatId, new UserData()); 
            sendMessage(chatId, "Please enter your username:");
        } else if ("login".equals(callbackData)) {
            sendMessage(chatId, "Login feature will be implemented soon.");
        }
    }

    private void processUserInput(Long chatId, String text) {
        UserState currentState = userStateMap.get(chatId);
        UserData userData = userDataMap.get(chatId);

        switch (currentState) {
            case AWAITING_USERNAME:
                userData.username = text;
                userStateMap.put(chatId, UserState.AWAITING_PASSWORD);
                sendMessage(chatId, "Great! Now enter your password:");
                break;

            case AWAITING_PASSWORD:
                userData.password = text;
                userStateMap.put(chatId, UserState.AWAITING_USERID);
                sendMessage(chatId, "Enter your User ID:");
                break;

            case AWAITING_USERID:
                userData.userId = text;
                userStateMap.put(chatId, UserState.AWAITING_EMAIL);
                sendMessage(chatId, "Enter your Email:");
                break;

            case AWAITING_EMAIL:
                userData.email = text;
                userStateMap.put(chatId, UserState.AWAITING_PHONE);
                sendMessage(chatId, "Enter your Phone Number:");
                break;

            case AWAITING_PHONE:
                userData.phone = text;
                userStateMap.put(chatId, UserState.COMPLETED);
                saveUserDataToDatabase(userData);
                sendMessage(chatId, "✅ Account created successfully! Thank you.");
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
                break;

            default:
                sendMessage(chatId, "Unknown state. Please start again.");
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
        }
    }

    private void saveUserDataToDatabase(UserData userData) {
        // TODO: Implement database saving logic (JPA or JDBC)
        System.out.println("Saving user: " + userData.username);
    }

    private void showOptions(Message message) {
        if (message == null || message.getChatId() == null) {
            System.out.println("Error: Invalid message or chatId.");
            return;
        }

        // Create Inline Keyboard Markup
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Create "Create Account" button
        InlineKeyboardButton createAccountButton = new InlineKeyboardButton();
        createAccountButton.setText("🆕 Create Account");
        createAccountButton.setCallbackData("create_account");

        // Create "Login" button
        InlineKeyboardButton loginButton = new InlineKeyboardButton();
        loginButton.setText("🔑 Login");
        loginButton.setCallbackData("login");

        // Add buttons to a row
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createAccountButton);
        rowInline.add(loginButton);

        // Add row to keyboard
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        // Send message with buttons
        SendMessage messageWithOptions = new SendMessage();
        messageWithOptions.setChatId(String.valueOf(message.getChatId())); 
        messageWithOptions.setText("Please select one of the options:");
        messageWithOptions.setReplyMarkup(markupInline);

        try {
            execute(messageWithOptions);  // Send message
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNameProvided(String text) {
        return text != null && !text.isEmpty();
    }

    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
