package com.SmartManagementSystem.SMMS.controller;

import com.SmartManagementSystem.SMMS.dto.CreateLocationRequest;
import com.SmartManagementSystem.SMMS.entity.Location;
import com.SmartManagementSystem.SMMS.exception.ResourceNotFoundException;
import com.SmartManagementSystem.SMMS.repository.LocationRepository;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public List<Location> listLocations(@AuthenticationPrincipal AuthenticatedUser user) {
        return locationRepository.findByOrganizationId(user.organizationId());
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@Valid @RequestBody CreateLocationRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser user) {
        if (!"ADMIN".equals(user.role()) && !"MANAGER".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getParentLocationId() != null) {
            locationRepository.findByIdAndOrganizationId(request.getParentLocationId(), user.organizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent location not found"));
        }

        Location location = new Location();
        location.setName(request.getName());
        location.setParentLocationId(request.getParentLocationId());
        location.setOrganizationId(user.organizationId());
        Location saved = locationRepository.save(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        if (!"ADMIN".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Location location = locationRepository.findByIdAndOrganizationId(id, user.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        locationRepository.delete(location);
        return ResponseEntity.noContent().build();
    }
}