package com.SmartManagementSystem.SMMS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNoteRequest {
    @NotBlank
    private String content;
}