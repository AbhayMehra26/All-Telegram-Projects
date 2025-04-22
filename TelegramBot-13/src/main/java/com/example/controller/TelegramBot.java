package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.example.model.Appointment;
import com.example.model.UserData;
import com.example.repository.AppointmentRepository;
import com.example.repository.UserRepository;
import com.example.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

@Service
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserService userService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Map<Long, String> userState = new HashMap<>();
    private Map<Long, UserData> tempUserData = new HashMap<>();
    private Map<Long, Long> userLastActiveTime = new HashMap<>();  // Tracks last user activity time
    private Map<Long, List<Integer>> userMessageIds = new HashMap<>();  // Stores message IDs for deletion
    private Map<Long, Integer> reminderCount = new HashMap<>();
    private final Map<Long, ScheduledFuture<?>> reminderTasks = new ConcurrentHashMap<>();


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();
            
            userLastActiveTime.put(chatId, System.currentTimeMillis());
            addMessageId(chatId, message.getMessageId()); // ✅ Correct, passing Integer (message ID)

                 if (text.equalsIgnoreCase("/start")) {
                sendTextMessage(chatId, "Hi dear, please send your name.");
                userState.put(chatId, "AWAITING_NAME");
                reminderCount.put(chatId, 0);// Reset counter
                startReminderTimer(chatId); // Start fresh timer
                
            } else if (userState.get(chatId) != null && userState.get(chatId).equals("AWAITING_NAME")) {
                sendOptions(chatId);
                userState.remove(chatId);
            } else if (userState.containsKey(chatId)) {
                handleUserInput(chatId, text);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

         // Track last user activity
            userLastActiveTime.put(chatId, System.currentTimeMillis());
            addMessageId(chatId,messageId);
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
                case "amc_service":
                    sendAppointmentLink(chatId, "AMC Service");
                    break;
                case "paid_service":
                    sendAppointmentLink(chatId, "Paid Service");
                    break;
                case "free_service":
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
                startReminderTimer(chatId);
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
                startReminderTimer(chatId);
            } else {
                sendTextMessage(chatId, "❌ Invalid password, please try again.");
            }
        }
        reminderCount.put(chatId, 0); // Reset reminder count on user response

    }

    public void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
      
        try {
            execute(message);  // ✅ This works because `execute()` is an instance method
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
        message.setText("Select a service:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("🔧 AMC Service").callbackData("amc_service").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("💰 Paid Service").callbackData("paid_service").build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder().text("🆓 Free Service").callbackData("free_service").build());

        keyboard.add(row1);
        keyboard.add(row2);
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
    	//String bookingUrl = "https://your-public-server.com/date-time/appointment?chatId=" + chatId + "&serviceName=" + serviceName.replace(" ", "%20");
    	//String bookingUrl = "http://localhost:8080/date-time/appointment?chatId=" + chatId + "&serviceName=" + serviceName.replace(" ", "%20");
    // String bookingUrl = "https://www.google.com" + chatId + "&serviceName=" + serviceName.replace(" ", "%20");
    	//String bookingUrl = "https://random-id.ngrok-free.app/date-time/appointment?chatId=" + chatId + "&serviceName=" + serviceName.replace(" ", "%20");
    //	String bookingUrl = "https://your-ngrok-id.ngrok-free.app/date-time/appointment?chatId=" + chatId + "&serviceName=" + serviceName.replace(" ", "%20");
    	String bookingUrl = "https://16a4-2409-4050-2d4a-f2f8-f94d-e299-349b-5.ngrok-free.app/date-time/appointment?chatId=" 
                + chatId + "&serviceName=" + serviceName.replace(" ", "%20");

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setParseMode("HTML");
        message.setText("📅 Click <a href=\"" + bookingUrl + "\">here</a> to book your appointment for " + serviceName + ".");
    
          // sendTextMessage(chatId, "Click this link: http://example.com");

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startReminderTimer(Long chatId) {
    	
    	// This line tries to get the number of reminders sent to a user (by their chatId).
    	//reminderCount is a Map<Long, Integer>.
    	//Reset reminder counter for this user
    	reminderCount.put(chatId, 0);
        
    	// Cancel previous task if already running
        if (reminderTasks.containsKey(chatId)) {
            reminderTasks.get(chatId).cancel(true);
            reminderTasks.remove(chatId);
        }
    	
      /*   It’s a method from the ScheduledExecutorService interface (typically used via Executors.newScheduledThreadPool(...)), and it's used to schedule tasks to run repeatedly at a fixed time interval.
           private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
           A scheduled task is started using scheduler.scheduleAtFixedRate, which runs every 30 seconds.
       */
        ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> 
        { 
        	/* 
        	    getOrDefault(chatId, 0) means:
        		If chatId exists in the map, return the stored value (reminder count).
        		If not, return 0 (default value).
        	*/
//       	    Example
        	/*			
                   Map<Long, Integer> reminderCount = new HashMap<>();
                   reminderCount.put(123L, 2);

				   int count = reminderCount.getOrDefault(123L, 0); // returns 2
				   int count2 = reminderCount.getOrDefault(456L, 0); // returns 0, because 456L doesn't exist
            */
        int count = reminderCount.getOrDefault(chatId, 0);
            
        //Similar idea — but this one tracks when the user was last active.
        //userLastActiveTime is a Map<Long, Long> — it stores timestamps.
          /*			
    	      getOrDefault(chatId, 0L) means 
			  If we have a recorded last active time, use it.
			  If not, use 0L (default timestamp = "never active").
    	  */
//     			 Example
           /*			
	               Map<Long, Long> userLastActiveTime = new HashMap<>();
                   userLastActiveTime.put(123L, 1712831220000L);                // some timestamp

                   long last = userLastActiveTime.getOrDefault(123L, 0L);       // returns stored timestamp
                   long last2 = userLastActiveTime.getOrDefault(456L, 0L);      // returns 0L
	        */
            long lastActive = userLastActiveTime.getOrDefault(chatId, 0L);
          
          //This gets the current time in milliseconds since January 1, 1970 UTC (called the "epoch").         
            long currentTime = System.currentTimeMillis();

            //This checks if 30 seconds have passed since the user last interacted.
            //If true, the bot considers the user "inactive."
            if (currentTime - lastActive >= 30000) { // 30 seconds of inactivity
                if (count < 3) 
                {
                	//The bot sends a reminder message.
                    sendTextMessage(chatId, "⏰ Reminder " + (count + 1) + ": Please respond to continue.");
                   //Increases the reminder count by 1.
                    reminderCount.put(chatId, count + 1);
                } else 
                {
                	//The bot gives a final warning.
                    sendTextMessage(chatId, "⚠️ You did not respond. Clearing chat and restarting session.");
                   // Deletes all previous messages using deleteAllMessages(chatId).
                    deleteAllMessages(chatId);
                  //  Resets the user’s session (state, temp data, etc.) using
                    resetUserSession(chatId);
                    
                 // Cancel and remove task after session reset
                    ScheduledFuture<?> task = reminderTasks.remove(chatId);
                    if (task != null) task.cancel(true);

                    reminderCount.remove(chatId);
                }
            }
        }, 30, 30, TimeUnit.SECONDS); 
      //Initial delay: 30 seconds (first check starts 30s after login).
      //  Period: every 30 seconds after that.
      //  So reminders go out after 30s, 60s, and 90s (if the user is inactive).
      
        // Save task to map
       reminderTasks.put(chatId, scheduledTask);
    }
   
    private void addMessageId(Long chatId, Integer messageId) {
        userMessageIds.putIfAbsent(chatId, new ArrayList<>());
        userMessageIds.get(chatId).add(messageId);
    }
  
 
    private void deleteAllMessages(Long chatId) {
        if (userMessageIds.containsKey(chatId)) {
            for (Integer messageId : userMessageIds.get(chatId)) {
                try {
                    execute(new DeleteMessage(chatId.toString(), messageId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            userMessageIds.remove(chatId);
        }
    }
    private void resetUserSession(Long chatId) {
        deleteAllMessages(chatId); // Deletes ALL previous messages
        reminderCount.remove(chatId);
        userState.remove(chatId);
        tempUserData.remove(chatId);
        userMessageIds.remove(chatId);
        userLastActiveTime.remove(chatId);  // Also clear last active time
     // ✅ Cancel and remove reminder task
        ScheduledFuture<?> task = reminderTasks.remove(chatId);
        if (task != null) task.cancel(true);
        
        sendTextMessage(chatId, "Session cleared. Please restart with /start.");
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