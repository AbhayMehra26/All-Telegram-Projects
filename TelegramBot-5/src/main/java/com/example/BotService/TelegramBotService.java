package com.example.BotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.example.model.UserData;
import com.example.model.UserState;
import com.example.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

	private final UserRepository userRepository;// Inject UserRepository
    private final Map<Long, UserState> userStateMap = new HashMap<>();
    private final Map<Long, UserData> userDataMap = new HashMap<>();
    private final Map<Long, String> userSelectedService = new HashMap<>();

 // Constructor injection ensures the dependency is properly assigned
    @Autowired
    public TelegramBotService(UserRepository userRepository) 
    {
        this.userRepository = userRepository;
    }

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
            sendMessage(chatId, "Hi dear, send me your name.",null);
        } else if (isValidName(text)) {
            showOptions(message);
        } else {
            sendMessage(chatId, "Please enter valid name  ",null);
        }
    }
    

    private void handleButtonClick(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        if ("create_account".equals(callbackData)) {
            userStateMap.put(chatId, UserState.AWAITING_USERNAME);
            userDataMap.put(chatId, new UserData()); 
            sendMessage(chatId, "Please enter your username:", null);
        } else if ("login".equals(callbackData)) {
            userStateMap.put(chatId, UserState.AWAITING_LOGIN_USERNAME);
            sendMessage(chatId, "Please enter your username:", null);
        }
        else if ("register_service".equals(callbackData)) {
            showServiceOptions(chatId); // Call method to show service options
        }
        else if ("amc_service".equals(callbackData) || "paid_service".equals(callbackData) || "free_service".equals(callbackData)) {
            userStateMap.put(chatId, UserState.AWAITING_APPOINTMENT);
            userSelectedService.put(chatId, callbackData);
            sendMessage(chatId, "Please enter your appointment date and time in (yyyy-MM-dd HH:mm) format:", null);
        }
     }
    private void processUserInput(Long chatId, String text) {
        UserState currentState = userStateMap.get(chatId);
        UserData userData = userDataMap.get(chatId);

        switch (currentState) {
            case AWAITING_USERNAME:
                userData.setUsername(text);
                userStateMap.put(chatId, UserState.AWAITING_PASSWORD);
                sendMessage(chatId, "Great! Now enter your password:", null);
                break;

            case AWAITING_PASSWORD:
                userData.setPassword(text);
                userStateMap.put(chatId, UserState.AWAITING_EMAIL);
                sendMessage(chatId, "Enter your Email:", null);
                break;

            case AWAITING_EMAIL:
                userData.setEmail(text);
                userStateMap.put(chatId, UserState.AWAITING_PHONE);
                sendMessage(chatId, "Enter your Phone Number:", null);
                break;

            case AWAITING_PHONE:
                userData.setPhone(text);
                userStateMap.put(chatId, UserState.COMPLETED);
                saveUserDataToDatabase(userData);
                sendMessage(chatId, "✅ Account created successfully! Thank you.", null);
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
                break;

            case AWAITING_LOGIN_USERNAME:
                userDataMap.put(chatId, new UserData());
                userDataMap.get(chatId).setUsername(text);
                userStateMap.put(chatId, UserState.AWAITING_LOGIN_PASSWORD);
                sendMessage(chatId, "Now, enter your password:", null);
                break;

            case AWAITING_LOGIN_PASSWORD:
                String username = userDataMap.get(chatId).getUsername();
                String password = text;
                Optional<UserData> user = userRepository.findByUsernameAndPassword(username, password);

                if (user.isPresent()) {
                    sendMessage(chatId, "✅ Login successful!", null);
                    showMainMenu(chatId);
                } else {
                    sendMessage(chatId, "❌ Incorrect username or password. Please try again.", null);
                }
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
                break;

            case AWAITING_APPOINTMENT:
                if (isValidDateTime(text)) {
                    String serviceType = userSelectedService.get(chatId);
                    sendMessage(chatId, "✅ Your " + serviceType.replace("_", " ") + " appointment is set for: " + text, null);
                } else {
                    sendMessage(chatId, "❌ Invalid format! Please enter the date and time in (yyyy-MM-dd HH:mm) format.", null);
                }
                userStateMap.remove(chatId);
                userSelectedService.remove(chatId);
                break;

            default:
                sendMessage(chatId, "Unknown state. Please start again.", null);
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
        }
    }
    
    private boolean isValidDateTime(String text) {
        try {
            LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    private void saveUserDataToDatabase(UserData userData) {
        userRepository.save(userData); // ✅ Correctly saves user data in the database
        System.out.println("✅ User saved: " + userData.getUsername());
    }

    private void showOptions(Message message) {
        if (message == null || message.getChatId() == null) {
            System.out.println("Error: Invalid message or chatId.");
            return;
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton createAccountButton = new InlineKeyboardButton();
        createAccountButton.setText("🆕 Create Account");
        createAccountButton.setCallbackData("create_account");

        InlineKeyboardButton loginButton = new InlineKeyboardButton();
        loginButton.setText("🔑 Login");
        loginButton.setCallbackData("login");

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createAccountButton);
        rowInline.add(loginButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        sendMessage(message.getChatId(), "Please select one of these options:", markupInline);
/*
        SendMessage messageWithOptions = new SendMessage();
        messageWithOptions.setChatId(String.valueOf(message.getChatId())); 
        messageWithOptions.setText("Please select one of the options:",);
        messageWithOptions.setReplyMarkup(markupInline);
         try {
            execute(messageWithOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
  }
    private void showMainMenu(Long chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton registerServiceButton = new InlineKeyboardButton();
        registerServiceButton.setText("📌 Registration for a Service");
        registerServiceButton.setCallbackData("register_service");

        InlineKeyboardButton serviceHistoryButton = new InlineKeyboardButton();
        serviceHistoryButton.setText("📜 See My Service History and Records");
        serviceHistoryButton.setCallbackData("service_history");

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(registerServiceButton);
        rowInline.add(serviceHistoryButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        sendMessage(chatId, "📌 Main Menu - Choose an option:", markupInline);
    }
//===================================== Show Keyboard Options =========================================================   

    private void showServiceOptions(Long chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton amcButton = new InlineKeyboardButton();
        amcButton.setText("🔧 AMC Service");
        amcButton.setCallbackData("amc_service");

        InlineKeyboardButton paidButton = new InlineKeyboardButton();
        paidButton.setText("💰 Paid Service");
        paidButton.setCallbackData("paid_service");

        InlineKeyboardButton freeButton = new InlineKeyboardButton();
        freeButton.setText("🆓 Free Service");
        freeButton.setCallbackData("free_service");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(amcButton);
        
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(paidButton);
        
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(freeButton);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);

        sendMessage(chatId, "Please select a service type:", markupInline);
    }

/*===================================== Show Keyboard Options =========================================================   
 
    private void showMainMenu(Long chatId) {
        // Create ReplyKeyboardMarkup
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true); // Make keyboard fit screen
        keyboardMarkup.setOneTimeKeyboard(true); // Hide after one use

        // Create a list to store keyboard rows
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // First row with menu options
        KeyboardRow row = new KeyboardRow();
        row.add("📌 Registration for a Service");
        row.add("📜 See My Service History and Records");

        // Add the row to the keyboard
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);

        // Send the menu message with the keyboard
        sendMessage(chatId, "📌 Main Menu - Choose an option:", keyboardMarkup);
    }
=================================================================================================== */ 

    private boolean isValidName(String text) {
        return text != null && text.matches("^[a-zA-Z\\s]+$"); // Accepts only letters and spaces
        
    }

    private void sendMessage(Long chatId, String messageText,ReplyKeyboard replyMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        if (replyMarkup != null) 
        {
            message.setReplyMarkup(replyMarkup);
        }
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
