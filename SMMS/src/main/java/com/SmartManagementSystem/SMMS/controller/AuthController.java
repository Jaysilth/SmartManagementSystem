package com.SmartManagementSystem.SMMS.controller;


import com.SmartManagementSystem.SMMS.dto.LoginRequest;
import com.SmartManagementSystem.SMMS.dto.LoginResponse;
import com.SmartManagementSystem.SMMS.dto.RegisterRequest;
import com.SmartManagementSystem.SMMS.dto.UserResponse;
import com.SmartManagementSystem.SMMS.entity.AppUser;
import com.SmartManagementSystem.SMMS.service.AuthService;
import com.SmartManagementSystem.SMMS.dto.CreateUserRequest;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = authService.register(request);
        return ResponseEntity.ok(UserResponse.from(user));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }


    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (!"ADMIN".equals(currentUser.role()) && !"MANAGER".equals(currentUser.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AppUser created = authService.createUserInOrg(request, currentUser.organizationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
    }
}
