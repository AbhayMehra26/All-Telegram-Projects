package com.example.BotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.example.model.UserData;
import com.example.model.UserState;
import com.example.repository.UserRepository;

import java.util.*;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

	private final UserRepository userRepository;// Inject UserRepository
    private final Map<Long, UserState> userStateMap = new HashMap<>();
    private final Map<Long, UserData> userDataMap = new HashMap<>();
    
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
        } else if (isNameProvided(text)) {
            showOptions(message);
        } else {
            sendMessage(chatId, "Please select an option: Create Account or Login.",null);
        }
    }

    private void handleButtonClick(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        if ("create_account".equals(callbackData)) {
            userStateMap.put(chatId, UserState.AWAITING_USERNAME);
            userDataMap.put(chatId, new UserData()); 
            sendMessage(chatId, "Please enter your username:",null);
        } else if ("login".equals(callbackData)) {
            sendMessage(chatId, "Login feature will be implemented soon.",null);
        }
    }

    private void processUserInput(Long chatId, String text) {
        UserState currentState = userStateMap.get(chatId);
        UserData userData = userDataMap.get(chatId);

        switch (currentState) {
            case AWAITING_USERNAME:
                userData.setUsername(text);
                userStateMap.put(chatId, UserState.AWAITING_PASSWORD);
                sendMessage(chatId, "Great! Now enter your password:",null);
                break;

            case AWAITING_PASSWORD:
                userData.setPassword(text);
                userStateMap.put(chatId, UserState.AWAITING_EMAIL);
                sendMessage(chatId, "Enter your Email:",null);
                break;

            case AWAITING_EMAIL:
                userData.setEmail(text);
                userStateMap.put(chatId, UserState.AWAITING_PHONE);
                sendMessage(chatId, "Enter your Phone Number:",null);
                break;

            case AWAITING_PHONE:
                userData.setPhone(text);
                userStateMap.put(chatId, UserState.COMPLETED);
                saveUserDataToDatabase(userData);
                sendMessage(chatId, "✅ Account created successfully! Thank you.",null);
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
                break;

            default:
                sendMessage(chatId, "Unknown state. Please start again.",null);
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
        }
    }

    private void saveUserDataToDatabase(UserData userData) {
        userRepository.save(userData); // Saves user data in the database
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

        sendMessage(message.getChatId(), "Please select one of the options:", markupInline);

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

    private boolean isNameProvided(String text) {
        return text != null && !text.isEmpty();
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
