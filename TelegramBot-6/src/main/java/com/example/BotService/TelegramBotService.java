package com.example.BotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TelegramBotService extends TelegramLongPollingBot 
{

    private final UserRepository userRepository;
    private final Map<Long, UserState> userStateMap = new HashMap<>();
    private final Map<Long, UserData> userDataMap = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//========================================================================================================================
    @Autowired
    public TelegramBotService(UserRepository userRepository) 
    {
        this.userRepository = userRepository;
    }
 //========================================================================================================================

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
//========================================================================================================================

    @Override
    public void onUpdateReceived(Update update) 
    {
        if (update.hasMessage() && update.getMessage().hasText()) 
        {
            handleTextMessage(update.getMessage());
        } 
        else if (update.hasCallbackQuery()) 
        {
            handleButtonClick(update.getCallbackQuery());
        }
    }
//========================================================================================================================
   ////The Message class represents a single message that a user sends in a chat.
	//It contains all the details about the message, such as:Who sent it (user details),What was sent (text, image, video, etc.),Where it was sent (private chat, group, or channel)
    private void handleTextMessage(Message message) 
    {
    // The bot needs chatId to send a response back to the correct user or group.
   // Without chatId, the bot would not know where to send messages. 	   
        Long chatId = message.getChatId();
   //getText() is a method of the Message class.
   //It returns the text content of the message that a user sent.
   //If the message contains only media (e.g., photo, video, document) without text, getText() will return null.
        String text = message.getText();

   //The containsKey() method is used with a Map (like HashMap, TreeMap, or LinkedHashMap) to check if a specific key exists in the map.
        if (userStateMap.containsKey(chatId))
        {
            processUserInput(chatId, text);
        } 
 
        else if (text.equals("/start")) 
        {
            sendMessage(chatId, "Hi dear, send me your name.", null);
        }        
        else if (isValidName(text)) 
        {
            showOptions(chatId);
        } else 
        {
            sendMessage(chatId, "Please enter a valid name.", null);
        }
    }

  //========================================================================================================================

    private void sendMessage(Long chatId, String text, ReplyKeyboard replyMarkup)
    {
    	// create  the SendMessage object
        SendMessage message = new SendMessage();
      //The bot needs chatId to send a response back to the correct user or group.
      // Without chatId, the bot would not know where to send messages.
        message.setChatId(String.valueOf(chatId)); // Convert chatId (Long) to String
        message.setText(text);

        if (replyMarkup != null) 
        {
            message.setReplyMarkup(replyMarkup);
        }

        try
        {
            execute(message);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }    
    
    
   //========================================================================================================================
  
    private void handleButtonClick(CallbackQuery callbackQuery)
    {
         //  Callback Data is hidden and sent to the bot when a button is clicked.
        // ✅ Used to identify actions instead of relying on button text.
        String callbackData = callbackQuery.getData();
        //The bot uses this chat ID to track the user's state and send responses.
        Long chatId = callbackQuery.getMessage().getChatId();
        
        switch (callbackData) 
        {
            case "create_account":
            	//The bot sets the user's state to UserState.AWAITING_USERNAME.
            	//This means the bot is now expecting the user to enter a username.
                userStateMap.put(chatId, UserState.AWAITING_USERNAME);
                //A new UserData object is created to store the registration details.
                userDataMap.put(chatId, new UserData());
                //The bot sends a message asking the user to provide a username.
                sendMessage(chatId, "Please enter your username:", null);
                break;

            case "login":
                userStateMap.put(chatId, UserState.AWAITING_LOGIN_USERNAME);
                sendMessage(chatId, "Please enter your username:", null);
                break;

                //he bot calls showServiceOptions(chatId), which displays buttons for different service types.
            case "register_service":
                showServiceOptions(chatId);
                break;

            case "amc_service":
            case "paid_service":
            case "free_service":
            	//The bot updates the state to AWAITING_APPOINTMENT, meaning the next user message is expected to be a date and time.
            	 userStateMap.put(chatId, UserState.AWAITING_APPOINTMENT);
                //It saves the selected service (amc_service, paid_service, free_service) in userDataMap.
            	// put() replaces existing values, but putIfAbsent() only adds if missing (avoiding overwriting existing data).
            	//This ensures a new UserData object is only created if the user does not have one already.
            	 userDataMap.putIfAbsent(chatId, new UserData());
                 //userDataMap.get(chatId) retrieves the UserData object for the user.
            	 //.setSelectedService(callbackData); updates the selectedService field with the value of callbackData (e.g., "amc_service", "paid_service", or "free_service").
            	 userDataMap.get(chatId).setSelectedService(callbackData);
                 sendMessage(chatId, "Please enter your appointment date and time (yyyy-MM-dd HH:mm):", null);
                 break;
        }
    }
 //========================================================================================================================
/*  
 What this method does:
  It processes text input from users based on their current state (UserState).
  It validates user inputs like usernames, passwords, emails, phone numbers, and appointment dates.
  It saves the user data into the database when all required details are provided.
 
 */
    private void processUserInput(Long chatId, String text) 
    {
    	//It retrieves the current state of the user (currentState).
        UserState currentState = userStateMap.get(chatId);
      // It gets the user’s stored data (userData).
        UserData userData = userDataMap.get(chatId);

        switch (currentState) 
        {
            case AWAITING_USERNAME:
            	//The bot checks the database (userRepository) to see if the username already exists.
            	//If findByUsername(text) returns a value (i.e., user already exists), the bot:
                if (userRepository.findByUsername(text).isPresent()) 
                {
                    sendMessage(chatId, "❌ Username already taken. Choose a different username.", null);
                    return;
                }
                
             //   ✔ If the username is available, the bot saves it in the UserData object associated with the user.
             //  ✔ userData is retrieved from userDataMap, where user details are temporarily stored.
             //  * Stores the new username in the UserData object.(Database)
                userData.setUsername(text);
             // The bot updates the user’s state in userStateMap to AWAITING_PASSWORD.
             //  This means the next message from this user will be treated as their password input.
               
             //if username is available then bot  ask for  the user to enter a password.
                userStateMap.put(chatId, UserState.AWAITING_PASSWORD);
              //✔ The bot will  sends a message to the user  enter their password.
                sendMessage(chatId, "Now enter your password:", null);
                break;

            case AWAITING_PASSWORD:
         /*   	
            * The passwordEncoder.encode(text) encrypts the password before storing it.
            * setPassword(...) saves the hashed password in the UserData object.
            * Security best practice: Storing plain-text passwords is dangerous.
            * BCrypt hashing makes passwords more secure and protects against data breaches.
           	📌 Example
            	🔹 User enters: mypassword123
                🔹 BCrypt generates:  $2a$10$sdf78y0v.VOJmJ2df33ldYXqP9R4h73OH3Vf3E4gKXnP1MgG/d8Kq
         * Even if you enter "mypassword123" again, it will generate a different hash next time because of salting.
       */    		
            	userData.setPassword(passwordEncoder.encode(text));
                //if the password is correct then bot will ask for your email  
                userStateMap.put(chatId, UserState.AWAITING_EMAIL);
                //The bot will send a message to the user  
                sendMessage(chatId, "Enter your Email:", null);
                break;

            case AWAITING_EMAIL:
            	// store the new email in the userData object (Database)
                userData.setEmail(text);
                //if saves the email then bot will ask for your phone number  
                userStateMap.put(chatId, UserState.AWAITING_PHONE);
               //The bot will send a message to the user  
                sendMessage(chatId, "Enter your Phone Number:", null);
                break;

            case AWAITING_PHONE:
            	// stores this phone number in the UserData object for the current user.

                userData.setPhone(text);
                
                //After collecting all required details (username, password, email, phone), the system saves the user data in the MySQL database.
                saveUserDataToDatabase(userData);
                //The bot will send a message to the user  
                sendMessage(chatId, "✅ Account created successfully! Thank you.", null);
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
                break;
/*      
🔹 Why remove userStateMap?

If we don’t remove it, the bot may still think the user is in the registration process.
Example issue:
The user completes registration.
Later, they send another message, and the bot mistakenly treats it as a phone number input.
✅ Removing userStateMap resets the user’s state so they can start fresh next time.

*/
                
/* 
🔹 Why remove userDataMap?

Data is already saved in the database, so keeping it in memory is unnecessary.
Prevents duplicate data if the user accidentally restarts registration.
Security reason → Avoid keeping sensitive user data (even temporarily stored, like the phone number).
✅ Removing userDataMap clears temporary user data once it is stored permanently.

*/
                
/*
                
🔹 Why Do We Remove userStateMap and userDataMap?

When the user completes the registration process, we remove their data from both maps to:

Free up memory → Prevent storing unnecessary data.
Reset the user's state → Allow new interactions without old data affecting them.
Ensure security → Avoid storing sensitive data longer than needed.

 */
               
                
            case AWAITING_APPOINTMENT:
                try 
                {
                    LocalDateTime appointmentDate = LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    userData.setAppointmentDate(appointmentDate);
                    saveUserDataToDatabase(userData);
                    sendMessage(chatId, "✅ Appointment successfully booked!", null);
                } 
                catch (DateTimeParseException e) 
                {
                    sendMessage(chatId, "❌ Invalid date format. Please enter the date and time in format (yyyy-MM-dd HH:mm):", null);
                    return;
                }
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
                break;

            case AWAITING_LOGIN_USERNAME:
                userDataMap.put(chatId, new UserData());
                userDataMap.get(chatId).setUsername(text);
                userStateMap.put(chatId, UserState.AWAITING_LOGIN_PASSWORD);
                sendMessage(chatId, "Now enter your password:", null);
                break;

            case AWAITING_LOGIN_PASSWORD:
                String username = userDataMap.get(chatId).getUsername();
                Optional<UserData> user = userRepository.findByUsername(username);

                if (user.isPresent() && passwordEncoder.matches(text, user.get().getPassword())) 
                {
                    sendMessage(chatId, "✅ Login successful!", null);
                    showMainMenu(chatId);
                } else 
                {
                    sendMessage(chatId, "❌ Incorrect username or password. Try again.", null);
                }
                userStateMap.remove(chatId);
                userDataMap.remove(chatId);
                break;
        }
    }
//========================================================================================================================
    
    private void saveUserDataToDatabase(UserData userData) 
    {
        try 
        {
            userRepository.save(userData);
        } 
        catch (Exception e) 
        {
            System.err.println("❌ Error saving user: " + e.getMessage());
        }
    }
    
    
//========================================================================================================================

    private InlineKeyboardMarkup createInlineKeyboard(List<List<InlineKeyboardButton>> buttons) 
    {
      // create object to Prepare the options buttons:
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
     // Attach the keyboard to markup
        markup.setKeyboard(buttons);
        return markup;
    }
    
//========================================================================================================================

    private InlineKeyboardButton createInlineButton(String text, String callbackData) 
    {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

 
//========================================================================================================================
    
    private void showOptions(Long chatId)
    {
        //List<List<InlineKeyboardButton>> → Represents List of rows in (keyboard structure)
        // InlineKeyboardButton → Represents a single row of buttons.
    	List<List<InlineKeyboardButton>> buttons = List.of(
                List.of(createInlineButton("🆕 Create Account", "create_account")),
                List.of(createInlineButton("🔑 Login", "login"))
        );

        sendMessage(chatId, "Please select an option:", createInlineKeyboard(buttons));
    }

//========================================================================================================================

    private void showMainMenu(Long chatId) 
    {
        List<List<InlineKeyboardButton>> buttons = List.of(
                List.of(createInlineButton("📌 Registration for a Service", "register_service")),
                List.of(createInlineButton("📂 Check My Service History", "service_history"))
        );

        sendMessage(chatId, "📌 Main Menu - Choose an option:", createInlineKeyboard(buttons));
    }
 //========================================================================================================================

    private void showServiceOptions(Long chatId) 
    {
        List<List<InlineKeyboardButton>> buttons = List.of(
                List.of(createInlineButton("🔧 AMC Service", "amc_service")),
                List.of(createInlineButton("💰 Paid Service", "paid_service")),
                List.of(createInlineButton("🆓 Free Service", "free_service"))
        );

        sendMessage(chatId, "Please select a service type:", createInlineKeyboard(buttons));
    }

 //========================================================================================================================

    private boolean isValidName(String text) 
    {
        return text.matches("^[a-zA-Z\\s]+$");
    }

}
