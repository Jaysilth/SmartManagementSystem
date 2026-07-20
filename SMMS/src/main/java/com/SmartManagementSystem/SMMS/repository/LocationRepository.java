package com.SmartManagementSystem.SMMS.repository;

import com.SmartManagementSystem.SMMS.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByOrganizationId(Long organizationId);
    Optional<Location> findByIdAndOrganizationId(Long id, Long organizationId);
}