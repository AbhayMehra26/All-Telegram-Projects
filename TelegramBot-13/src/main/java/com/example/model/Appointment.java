package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // Store the user's username
    private String serviceName;
    private LocalDateTime appointmentDateTime;
    private int reminderCount = 0;  // Default 0
    private int lastMessageId;  // Store last message ID for deletion
    private String chatId;
    
    
    // Constructors
    public Appointment() {}
 
    
    // ✅ Add this constructor for easier creation of appointments
    public Appointment(String chatId, String serviceName, LocalDateTime appointmentDateTime) {
        this.chatId = chatId;
        this.serviceName = serviceName;
        this.appointmentDateTime = appointmentDateTime;
        this.reminderCount = 0;  // Default to 0
        this.lastMessageId = 0;  // Default to 0
    }

	// Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }


	public int getReminderCount() {
		return reminderCount;
	}


	public void setReminderCount(int reminderCount) {
		this.reminderCount = reminderCount;
	}


	public int getLastMessageId() {
		return lastMessageId;
	}


	public void setLastMessageId(int lastMessageId) {
		this.lastMessageId = lastMessageId;
	}

	// ✅ Getters and Setters
    public String getChatId() {
        return chatId;
    }
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

}
