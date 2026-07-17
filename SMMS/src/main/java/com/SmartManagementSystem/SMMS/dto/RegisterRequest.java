package com.SmartManagementSystem.SMMS.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String organizationName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String role; // ADMIN, MANAGER, TECHNICIAN, REQUESTER — validated below

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}