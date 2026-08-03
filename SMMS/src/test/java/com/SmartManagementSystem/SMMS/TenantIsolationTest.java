package com.SmartManagementSystem.SMMS;

import com.SmartManagementSystem.SMMS.dto.CreateTicketRequest;
import com.SmartManagementSystem.SMMS.dto.LoginRequest;
import com.SmartManagementSystem.SMMS.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TenantIsolationTest extends AbstractIntegrationTest {



    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAndLogin(String orgName, String email, String password) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setOrganizationName(orgName);
        register.setEmail(email);
        register.setPassword(password);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void ticketsAreIsolatedBetweenOrganizations() throws Exception {
        String tokenOrgA = registerAndLogin("Org A", "admin-a@test.com", "password123");
        String tokenOrgB = registerAndLogin("Org B", "admin-b@test.com", "password123");

        CreateTicketRequest ticket = new CreateTicketRequest();
        ticket.setTitle("Org A's private ticket");

        mockMvc.perform(post("/api/v1/tickets")
                .header("Authorization", "Bearer " + tokenOrgA)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(ticket)));

        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + tokenOrgB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}