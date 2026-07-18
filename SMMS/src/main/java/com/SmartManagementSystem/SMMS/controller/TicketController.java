package com.SmartManagementSystem.SMMS.controller;

import com.SmartManagementSystem.SMMS.dto.AssignTicketRequest;
import com.SmartManagementSystem.SMMS.dto.CreateTicketRequest;
import com.SmartManagementSystem.SMMS.dto.UpdateStatusRequest;
import com.SmartManagementSystem.SMMS.entity.Ticket;
import com.SmartManagementSystem.SMMS.repository.TicketRepository;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public List<Ticket> getTickets(@AuthenticationPrincipal AuthenticatedUser user) {
        if ("REQUESTER".equals(user.role())) {
            return ticketRepository.findByOrganizationIdAndCreatedBy(user.organizationId(), user.userId());
        }
        return ticketRepository.findByOrganizationId(user.organizationId());
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody CreateTicketRequest request,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        if ("TECHNICIAN".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setOrganizationId(user.organizationId());
        ticket.setCreatedBy(user.userId());
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
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

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
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        boolean isAssignedTechnician = "TECHNICIAN".equals(user.role())
                && user.userId().equals(ticket.getAssignedTechnicianId());
        boolean isManagerOrAdmin = "MANAGER".equals(user.role()) || "ADMIN".equals(user.role());

        if (!isAssignedTechnician && !isManagerOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ticket.setStatus(request.getStatus().toUpperCase());
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(saved);
    }

}