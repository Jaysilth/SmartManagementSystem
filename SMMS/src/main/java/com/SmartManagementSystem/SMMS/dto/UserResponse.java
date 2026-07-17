package com.SmartManagementSystem.SMMS.dto;



import com.SmartManagementSystem.SMMS.entity.AppUser;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String role;
    private Long organizationId;

    public static UserResponse from(AppUser user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setOrganizationId(user.getOrganizationId());
        return dto;
    }
}