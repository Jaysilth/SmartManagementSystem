package com.SmartManagementSystem.SMMS;

import com.SmartManagementSystem.SMMS.dto.CreateTicketRequest;
import com.SmartManagementSystem.SMMS.dto.LoginRequest;
import com.SmartManagementSystem.SMMS.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RbacTest extends AbstractIntegrationTest {

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

    private String createUserInOrg(String adminToken, String email, String password, String role) throws Exception {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}", email, password, role);

        mockMvc.perform(post("/api/v1/auth/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(body));

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
    void technicianCannotCreateTicket() throws Exception {
        String adminToken = registerAndLogin("RBAC Test Org", "rbac-admin@test.com", "password123");
        String technicianToken = createUserInOrg(adminToken, "rbac-tech@test.com", "password123", "TECHNICIAN");

        CreateTicketRequest ticket = new CreateTicketRequest();
        ticket.setTitle("Technician should not be able to create this");

        mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ticket)))
                .andExpect(status().isForbidden());
    }
}