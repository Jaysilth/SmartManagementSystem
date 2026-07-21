package com.SmartManagementSystem.SMMS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTicketRequest {
    @NotBlank
    private String title;

    private Long departmentId;
    private Long locationId;

    private String description;
    private String category;
    private String priority;
}