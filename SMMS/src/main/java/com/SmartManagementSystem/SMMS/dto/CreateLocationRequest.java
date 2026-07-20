package com.SmartManagementSystem.SMMS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLocationRequest {
    @NotBlank
    private String name;

    private Long parentLocationId;
}