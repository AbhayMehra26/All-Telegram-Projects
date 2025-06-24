package com.example.model;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
	public class Appointment {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private Long chatId;
	    private String username;  // optional, you can link with UserData if you want

	    private String serviceName;
	    private LocalDateTime appointmentDateTime;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Long getChatId() {
			return chatId;
		}
		public void setChatId(Long chatId) {
			this.chatId = chatId;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getServiceName() {
			return serviceName;
		}
		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}
		public LocalDateTime getAppointmentDateTime() {
			return appointmentDateTime;
		}
		public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
			this.appointmentDateTime = appointmentDateTime;
		}

	    
	    
}
