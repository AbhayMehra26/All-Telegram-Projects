package com.example.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    @Column(unique = true)
    
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
   
    private String password;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", 
    		 message = "Email must be in a valid format (e.g., example@email.com).") 
    @Column(unique = true)
   
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[0-9]{10}$",
    		 message = "Phone number must be exactly 10 digits.")
 
    private String phone;
    
    
    private String selectedService; // ✅ New field added
    private LocalDateTime appointmentDate; // ✅ New field added


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSelectedService() {
		return selectedService;
	}

	public void setSelectedService(String selectedService) {
		this.selectedService = selectedService;
	}

	public LocalDateTime getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDateTime appointmentDate) {
		this.appointmentDate = appointmentDate;
	}
    
    
}
