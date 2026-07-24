package com.SmartManagementSystem.SMMS.controller;

import com.SmartManagementSystem.SMMS.dto.DashboardResponse;
import com.SmartManagementSystem.SMMS.repository.TicketRepository;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final TicketRepository ticketRepository;

    public DashboardController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal AuthenticatedUser user) {
        if (!"ADMIN".equals(user.role()) && !"MANAGER".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        long openCount = ticketRepository.countOpen(user.organizationId());

        List<Object[]> rawCounts = ticketRepository.countByStatus(user.organizationId());
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : rawCounts) {
            byStatus.put((String) row[0], (Long) row[1]);
        }

        return ResponseEntity.ok(new DashboardResponse(openCount, byStatus));
    }
}