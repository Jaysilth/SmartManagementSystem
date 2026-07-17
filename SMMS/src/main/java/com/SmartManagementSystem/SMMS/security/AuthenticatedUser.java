package com.SmartManagementSystem.SMMS.security;

public record AuthenticatedUser(Long userId, Long organizationId, String role) {}
