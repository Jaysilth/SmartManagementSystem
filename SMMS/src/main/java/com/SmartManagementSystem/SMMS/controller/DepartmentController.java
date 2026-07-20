package com.SmartManagementSystem.SMMS.controller;

import com.SmartManagementSystem.SMMS.dto.CreateDepartmentRequest;
import com.SmartManagementSystem.SMMS.entity.Department;
import com.SmartManagementSystem.SMMS.exception.ResourceNotFoundException;
import com.SmartManagementSystem.SMMS.repository.DepartmentRepository;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public List<Department> listDepartments(@AuthenticationPrincipal AuthenticatedUser user) {
        return departmentRepository.findByOrganizationId(user.organizationId());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody CreateDepartmentRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        if (!"ADMIN".equals(user.role()) && !"MANAGER".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Department department = new Department();
        department.setName(request.getName());
        department.setOrganizationId(user.organizationId());
        Department saved = departmentRepository.save(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        if (!"ADMIN".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Department department = departmentRepository.findByIdAndOrganizationId(id, user.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        departmentRepository.delete(department);
        return ResponseEntity.noContent().build();
    }
}