package com.SmartManagementSystem.SMMS.controller;

import com.SmartManagementSystem.SMMS.dto.AssignTicketRequest;
import com.SmartManagementSystem.SMMS.dto.CreateTicketRequest;
import com.SmartManagementSystem.SMMS.dto.UpdateStatusRequest;
import com.SmartManagementSystem.SMMS.entity.Ticket;
import com.SmartManagementSystem.SMMS.repository.DepartmentRepository;
import com.SmartManagementSystem.SMMS.repository.LocationRepository;
import com.SmartManagementSystem.SMMS.repository.TicketRepository;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.SmartManagementSystem.SMMS.exception.ResourceNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;

    public TicketController(TicketRepository ticketRepository,
                            DepartmentRepository departmentRepository,
                            LocationRepository locationRepository) {
        this.ticketRepository = ticketRepository;
        this.departmentRepository = departmentRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public List<Ticket> getTickets(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) String priority,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) String search,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        Long createdBy = "REQUESTER".equals(user.role()) ? user.userId() : null;
        return ticketRepository.search(user.organizationId(), createdBy, status, priority, category, search);
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody CreateTicketRequest request,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        if ("TECHNICIAN".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getDepartmentId() != null) {
            departmentRepository.findByIdAndOrganizationId(request.getDepartmentId(), user.organizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }
        if (request.getLocationId() != null) {
            locationRepository.findByIdAndOrganizationId(request.getLocationId(), user.organizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setOrganizationId(user.organizationId());
        ticket.setCreatedBy(user.userId());
        ticket.setDepartmentId(request.getDepartmentId());
        ticket.setLocationId(request.getLocationId());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority() != null ? request.getPriority().toUpperCase() : "MEDIUM");
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Ticket> assignTicket(@PathVariable Long id,
                                               @Valid @RequestBody AssignTicketRequest request,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        if (!"ADMIN".equals(user.role()) && !"MANAGER".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Ticket ticket = ticketRepository.findByIdAndOrganizationId(id, user.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setAssignedTechnicianId(request.getTechnicianId());
        ticket.setStatus("ASSIGNED");
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(saved);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> updateStatus(@PathVariable Long id,
                                               @Valid @RequestBody UpdateStatusRequest request,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        Ticket ticket = ticketRepository.findByIdAndOrganizationId(id, user.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        boolean isAssignedTechnician = "TECHNICIAN".equals(user.role())
                && user.userId().equals(ticket.getAssignedTechnicianId());
        boolean isManagerOrAdmin = "MANAGER".equals(user.role()) || "ADMIN".equals(user.role());
        boolean isOwningRequester = "REQUESTER".equals(user.role())
                && user.userId().equals(ticket.getCreatedBy());

        String newStatus = request.getStatus().toUpperCase();

        if (isOwningRequester) {
            boolean validRequesterTransition =
                    "CLOSED".equals(newStatus) ||
                            "CANCELLED".equals(newStatus) ||
                            ("REOPENED".equals(newStatus) && "COMPLETED".equals(ticket.getStatus()));
            if (!validRequesterTransition) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (!isAssignedTechnician && !isManagerOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(saved);
    }



}