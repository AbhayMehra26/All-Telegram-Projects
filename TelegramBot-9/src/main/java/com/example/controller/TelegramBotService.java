package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.example.model.Appointment;
import com.example.model.UserData;
import com.example.model.UserState;
import com.example.repository.AppointmentRepository;
import com.example.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    
//=========================================================================================================
    @Override
    public String getBotUsername() 
    {
        return "teleai23bot";
    }

    @Override
    public String getBotToken() 
    {
        return "7387064131:AAECT37CkUpHLW9GwCz27IVN3NQ9AOr_2_A";
    }
//=========================================================================================================

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String userMessage = update.getMessage().getText();

            if (userMessage.equalsIgnoreCase("/start")) {
                sendTextMessage(chatId, "Hi dear, please send your name.");
                return;
            }
            //Retrieves the current state of the user from the UserState class.
            String userState = UserState.getUserState(chatId);

            if ("WAITING_FOR_USERNAME".equals(userState)) {
                handleUsernameInput(chatId, userMessage);
            } else if ("WAITING_FOR_PASSWORD".equals(userState)) {
                handlePasswordInput(chatId, userMessage);
            } else if ("WAITING_FOR_EMAIL".equals(userState)) {
                handleEmailInput(chatId, userMessage);
            } else if ("WAITING_FOR_PHONE".equals(userState)) {
                handlePhoneInput(chatId, userMessage);
            } // Add these blocks for login states:
            else if ("WAITING_FOR_LOGIN_USERNAME".equals(userState)) {
                handleLoginUsername(chatId, userMessage);
            } else if ("WAITING_FOR_LOGIN_PASSWORD".equals(userState)) {
                handleLoginPassword(chatId, userMessage);
            } else {
                sendOptionsMessage(chatId);
            }
        } 
        // Handling Button Clicks
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if ("CREATE_ACCOUNT".equals(callbackData)) {
                sendTextMessage(chatId, "Please enter a username:");
                UserState.setUserState(chatId, "WAITING_FOR_USERNAME");
            }
            else if ("LOGIN".equals(callbackData)) {
                sendTextMessage(chatId, "Please enter your username:");
                UserState.setUserState(chatId, "WAITING_FOR_LOGIN_USERNAME");
            }else if ("REGISTER_SERVICE".equals(callbackData)) {
                sendServiceSelectionMessage(chatId);
            } 
            else if ("AMC_SERVICE".equals(callbackData) || 
                     "PAID_SERVICE".equals(callbackData) || 
                     "FREE_SERVICE".equals(callbackData)) {
                handleServiceSelection(chatId, callbackData);
            }
            
        }
    }
//=========================================================================================================

    private void sendTextMessage(Long chatId, String text) 
    {
    	//Creates a new SendMessage object, which is used to send messages to the user.
        SendMessage message = new SendMessage();
        //The chatId uniquely identifies a chat between the bot and a user.
        //Converts the chatId (which is a Long type) into a String (because setChatId() requires a String).
        message.setChatId(chatId.toString());
        //This sets the actual text of the message that the bot will send.
        message.setText(text);
      //execute(message); → Sends the message to the Telegram API. 
      //If an error occurs, it prints the exception for debugging.
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
//=========================================================================================================
  
    private void handleUsernameInput(Long chatId, String username)
    {
    	// Validate the username using regex
    	if (!Pattern.matches("^[a-zA-Z0-9_]{3,15}$", username)) {
            sendTextMessage(chatId, "Invalid username! Only letters, numbers, and _ allowed (3-15 chars). Try again:");
            return;
        }
           //userRepository.existsByUsername(username) is a Spring Data JPA method that checks if a user with the given username already exists in the database.
        if (userRepository.existsByUsername(username)) {
            sendTextMessage(chatId, "This username is already taken. Please choose another:");
            return;
        }
        // Store the username in the temporary user data
        UserData userData = new UserData();
        userData.setUsername(username);
     // Save the updated data
        UserState.setTempUserData(chatId, userData);
        // Move to the next step (ask for a password
        sendTextMessage(chatId, "Username saved! Now, enter a password:");
        UserState.setUserState(chatId, "WAITING_FOR_PASSWORD");

    }

//=========================================================================================================

    private void handlePasswordInput(Long chatId, String password) {
        if (!Pattern.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", password)) {
            sendTextMessage(chatId, "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character");
            return;
        }

        UserData userData = UserState.getTempUserData(chatId);
        userData.setPassword(password);
        sendTextMessage(chatId, "Password saved! Now, enter your email:");
        UserState.setUserState(chatId, "WAITING_FOR_EMAIL");

    }
//=========================================================================================================

    private void handleEmailInput(Long chatId, String email) {
        if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email)) {
            sendTextMessage(chatId, "Invalid email! Enter a valid email address:");
            return;
        }

        if (userRepository.existsByEmail(email)) {
            sendTextMessage(chatId, "This email is already registered. Enter another:");
            return;
        }

        UserData userData = UserState.getTempUserData(chatId);
        userData.setEmail(email);
        UserState.setUserState(chatId, "WAITING_FOR_PHONE");

        sendTextMessage(chatId, "Email saved! Now, enter your phone number:");
    }
//=========================================================================================================

    private void handlePhoneInput(Long chatId, String phone) {
         // Validate the phone number using regex
    	if (!Pattern.matches("^[0-9]{10}$", phone)) {
            sendTextMessage(chatId, "Invalid phone number! Enter a 10-digit number:");
            return;// Stop further execution
        }

        if (userRepository.existsByPhone(phone)) {
            sendTextMessage(chatId, "This phone number is already registered. Enter another:");
            return;
        }
        
        
        
    // Store the phone number in the temporary user data
        UserData userData = UserState.getTempUserData(chatId);
        if (userData == null) {
            userData = new UserData(); // Initialize new UserData if not found
        }
        
        userData.setChatId(chatId);
        userData.setPhone(phone);

        // Save user to database
        userRepository.save(userData);
        UserState.clearUserState(chatId);
                sendTextMessage(chatId, "✅ Account created successfully!");
}

//=========================================================================================================

    private void sendOptionsMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please select one of these options:");

        // Create Inline Keyboard
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton createAccountButton = new InlineKeyboardButton();
        createAccountButton.setText("Create Account");
        createAccountButton.setCallbackData("CREATE_ACCOUNT");

        InlineKeyboardButton loginButton = new InlineKeyboardButton();
        loginButton.setText("Login");
        loginButton.setCallbackData("LOGIN");

        row1.add(createAccountButton);
        row1.add(loginButton);
        rows.add(row1);

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
  //===================================================================================
    private void handleLoginUsername(Long chatId, String username) {
        UserData user = userRepository.findByUsername(username);
        
        if (user == null) {
            sendTextMessage(chatId, "❌ Username is wrong, please try again:");
            return;
        }
        
        // Store the user in temporary session
        UserState.setTempUserData(chatId, user);
        
        sendTextMessage(chatId, "✅ Username found! Now, enter your password:");
        UserState.setUserState(chatId, "WAITING_FOR_LOGIN_PASSWORD");
    }

 //===================================================================================
    private void handleLoginPassword(Long chatId, String password) {
        UserData user = UserState.getTempUserData(chatId);

        if (user == null) {
            sendTextMessage(chatId, "❌ Please start the login process again.");
            sendOptionsMessage(chatId);
            return;
        }

        if (!user.getPassword().equals(password)) {
            sendTextMessage(chatId, "❌ Invalid password, please try again:");
            return;
        }

        sendTextMessage(chatId, "✅ Login successful! Select an option:");
        sendServiceOptions(chatId);
        UserState.clearUserState(chatId);
    }

 //===================================================================================
    private void sendServiceOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please select one of these options:");

        // Create Inline Keyboard
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        
        InlineKeyboardButton registerServiceButton = new InlineKeyboardButton();
        registerServiceButton.setText("Registration for a Service");
        registerServiceButton.setCallbackData("REGISTER_SERVICE");

        InlineKeyboardButton viewHistoryButton = new InlineKeyboardButton();
        viewHistoryButton.setText("See my service history and records");
        viewHistoryButton.setCallbackData("VIEW_HISTORY");

        row1.add(registerServiceButton);
        row1.add(viewHistoryButton);
        rows.add(row1);

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
//===================================================================================
    private void sendServiceSelectionMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please select a service:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton amcButton = new InlineKeyboardButton();
        amcButton.setText("AMC Service");
        amcButton.setCallbackData("AMC_SERVICE");

        InlineKeyboardButton paidButton = new InlineKeyboardButton();
        paidButton.setText("Paid Service");
        paidButton.setCallbackData("PAID_SERVICE");

        InlineKeyboardButton freeButton = new InlineKeyboardButton();
        freeButton.setText("Free Service");
        freeButton.setCallbackData("FREE_SERVICE");

        row1.add(amcButton);
        row1.add(paidButton);
        row1.add(freeButton);
        rows.add(row1);

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
//===================================================================================

    private void handleServiceSelection(Long chatId, String serviceType) {
        String serviceName;
        switch (serviceType) {
            case "AMC_SERVICE":
                serviceName = "AMC Service";
                break;
            case "PAID_SERVICE":
                serviceName = "Paid Service";
                break;
            case "FREE_SERVICE":
                serviceName = "Free Service";
                break;
            default:
                sendTextMessage(chatId, "Invalid selection. Please try again.");
                return;
        }

        UserState.setUserState(chatId, "WAITING_FOR_APPOINTMENT");

     // External web page for selecting date and time
      //  String bookingUrl = "http://localhost:8080/book-appointment?chatId=" + chatId + "&service=" + serviceName;
      //  sendTextMessage(chatId, "Click the link below to select your appointment date and time:\n" + bookingUrl);
      //  String bookingUrl = "http://localhost:8080/appointment/book-appointment?chatId=" + chatId + "&service=" + serviceName;
       // sendTextMessage(chatId, "Click the link below to select your appointment date and time:\n" + bookingUrl);
    
        
        
        // Check if running locally
        boolean isLocal = true; // Change this to false when deployed online

        if (isLocal) {
            sendTextMessage(chatId, "⛔ The booking page is currently unavailable because the server is running locally.\n"
                    + "Please deploy the server online to generate a valid booking link.");
        } else {
            String bookingUrl = "http://telegrambot9.render.com/book-appointment?chatId=" + chatId + "&service=" + serviceName;
            sendTextMessage(chatId, "Click the link below to select your appointment date and time:\n" + bookingUrl);
        }
    }
    
//===================================================================================
    public void confirmAppointment(Long chatId, String service, String dateTime) {
        UserData user = userRepository.findByChatId(chatId);

        if (user != null) {
            Appointment appointment = new Appointment();
            appointment.setUser(user);
            appointment.setService(service);
            appointment.setDateTime(dateTime);
            appointmentRepository.save(appointment);

            sendTextMessage(chatId, "✅ Your appointment for " + service + " is booked on " + dateTime + ".");
        } else {
            sendTextMessage(chatId, "⚠️ Error booking appointment. Please try again.");
        }
    }

}
