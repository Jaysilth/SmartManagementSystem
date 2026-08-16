package com.SmartManagementSystem.SMMS;

import com.SmartManagementSystem.SMMS.dto.LoginRequest;
import com.SmartManagementSystem.SMMS.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthTest extends AbstractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setOrganizationName("Wrong Password Org");
        register.setEmail("wrongpass@test.com");
        register.setPassword("correctpassword123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest badLogin = new LoginRequest();
        badLogin.setEmail("wrongpass@test.com");
        badLogin.setPassword("totallywrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registeringDuplicateEmailReturns409() throws Exception {
        RegisterRequest first = new RegisterRequest();
        first.setOrganizationName("First Org");
        first.setEmail("duplicate@test.com");
        first.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(first)));

        RegisterRequest duplicate = new RegisterRequest();
        duplicate.setOrganizationName("Second Org");
        duplicate.setEmail("duplicate@test.com");
        duplicate.setPassword("differentpassword123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }
}