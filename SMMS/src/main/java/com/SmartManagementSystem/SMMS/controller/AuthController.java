package com.SmartManagementSystem.SMMS.controller;


import com.SmartManagementSystem.SMMS.dto.LoginRequest;
import com.SmartManagementSystem.SMMS.dto.LoginResponse;
import com.SmartManagementSystem.SMMS.dto.RegisterRequest;
import com.SmartManagementSystem.SMMS.dto.UserResponse;
import com.SmartManagementSystem.SMMS.entity.AppUser;
import com.SmartManagementSystem.SMMS.repository.AppUserRepository;
import com.SmartManagementSystem.SMMS.service.AuthService;
import com.SmartManagementSystem.SMMS.dto.CreateUserRequest;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository appUserRepository;

    public AuthController(AuthService authService, AppUserRepository appUserRepository) {
        this.authService = authService;
        this.appUserRepository = appUserRepository;

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
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers(@RequestParam(required = false) String role,
                                                        @AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (!"ADMIN".equals(currentUser.role()) && !"MANAGER".equals(currentUser.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AppUser> users = role != null
                ? appUserRepository.findByOrganizationIdAndRole(currentUser.organizationId(), role.toUpperCase())
                : appUserRepository.findByOrganizationId(currentUser.organizationId());
        return ResponseEntity.ok(users.stream().map(UserResponse::from).collect(Collectors.toList()));
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
