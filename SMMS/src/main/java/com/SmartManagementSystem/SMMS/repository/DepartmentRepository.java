package com.SmartManagementSystem.SMMS.repository;

import com.SmartManagementSystem.SMMS.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByOrganizationId(Long organizationId);
    Optional<Department> findByIdAndOrganizationId(Long id, Long organizationId);
}