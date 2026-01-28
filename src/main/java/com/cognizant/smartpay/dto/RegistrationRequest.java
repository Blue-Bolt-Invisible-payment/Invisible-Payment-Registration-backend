package com.cognizant.smartpay.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for user registration request
 */
@Data
public class RegistrationRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Date of birth is required")
    private String dob;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;
    
    @NotNull(message = "Initial wallet balance is required")
    @DecimalMin(value = "0.0", message = "Initial balance must be non-negative")
    private BigDecimal initialWalletBalance;
    
    @NotNull(message = "Fingerprint data is required")
    private Map<String, Object> fingerprintData;
    
    @NotBlank(message = "Device type is required")
    private String deviceType;
    
    @NotBlank(message = "Enrollment timestamp is required")
    private String enrolledAt;
}
