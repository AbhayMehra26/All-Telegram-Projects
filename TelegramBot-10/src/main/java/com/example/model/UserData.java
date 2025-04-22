package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long chatId;  // Telegram Chat ID
    
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", 
             message = "Email must be in a valid format (e.g., example@email.com).") 
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[0-9]{10}$",
             message = "Phone number must be exactly 10 digits.")
    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String service = "DEFAULT_SERVICE";

    
    
    
	public UserData() {
		super();
	}

	public UserData(Long chatId,String service,
			@NotBlank(message = "Username is required.") @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.") String username,
			@NotBlank(message = "Password is required.") @Size(min = 6, message = "Password must be at least 6 characters long.") String password,
			@NotBlank(message = "Email is required.") @Email(message = "Invalid email format.") @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Email must be in a valid format (e.g., example@email.com).") String email,
			@NotBlank(message = "Phone number is required.") @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits.") String phone) {
		super();
		this.chatId = chatId;
		this.username = username;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.service = service;

	}

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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
    
 
    
}