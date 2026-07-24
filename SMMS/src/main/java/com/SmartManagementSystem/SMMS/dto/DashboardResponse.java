package com.SmartManagementSystem.SMMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private long openCount;
    private Map<String, Long> byStatus;
}
