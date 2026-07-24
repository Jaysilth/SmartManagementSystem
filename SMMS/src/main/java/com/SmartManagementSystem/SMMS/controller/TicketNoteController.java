package com.SmartManagementSystem.SMMS.controller;

import com.SmartManagementSystem.SMMS.dto.CreateNoteRequest;
import com.SmartManagementSystem.SMMS.entity.TicketComment;
import com.SmartManagementSystem.SMMS.entity.WorkNote;
import com.SmartManagementSystem.SMMS.exception.ResourceNotFoundException;
import com.SmartManagementSystem.SMMS.repository.TicketCommentRepository;
import com.SmartManagementSystem.SMMS.repository.TicketRepository;
import com.SmartManagementSystem.SMMS.repository.WorkNoteRepository;
import com.SmartManagementSystem.SMMS.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}")
public class TicketNoteController {

    private final TicketRepository ticketRepository;
    private final WorkNoteRepository workNoteRepository;
    private final TicketCommentRepository ticketCommentRepository;

    public TicketNoteController(TicketRepository ticketRepository,
                                WorkNoteRepository workNoteRepository,
                                TicketCommentRepository ticketCommentRepository) {
        this.ticketRepository = ticketRepository;
        this.workNoteRepository = workNoteRepository;
        this.ticketCommentRepository = ticketCommentRepository;
    }

    private void verifyTicketInOrg(Long ticketId, Long organizationId) {
        ticketRepository.findByIdAndOrganizationId(ticketId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    // --- Work notes: Technician/Manager/Admin only, hidden from Requester ---

    @GetMapping("/work-notes")
    public ResponseEntity<List<WorkNote>> getWorkNotes(@PathVariable Long ticketId,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        if ("REQUESTER".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        verifyTicketInOrg(ticketId, user.organizationId());
        return ResponseEntity.ok(
                workNoteRepository.findByTicketIdAndOrganizationIdOrderByCreatedAtAsc(ticketId, user.organizationId()));
    }

    @PostMapping("/work-notes")
    public ResponseEntity<WorkNote> createWorkNote(@PathVariable Long ticketId,
                                                   @Valid @RequestBody CreateNoteRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser user) {
        if ("REQUESTER".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        verifyTicketInOrg(ticketId, user.organizationId());

        WorkNote note = new WorkNote();
        note.setTicketId(ticketId);
        note.setAuthorId(user.userId());
        note.setContent(request.getContent());
        note.setOrganizationId(user.organizationId());
        WorkNote saved = workNoteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // --- Comments: all roles, no restriction ---

    @GetMapping("/comments")
    public ResponseEntity<List<TicketComment>> getComments(@PathVariable Long ticketId,
                                                           @AuthenticationPrincipal AuthenticatedUser user) {
        verifyTicketInOrg(ticketId, user.organizationId());
        return ResponseEntity.ok(
                ticketCommentRepository.findByTicketIdAndOrganizationIdOrderByCreatedAtAsc(ticketId, user.organizationId()));
    }

    @PostMapping("/comments")
    public ResponseEntity<TicketComment> createComment(@PathVariable Long ticketId,
                                                       @Valid @RequestBody CreateNoteRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        verifyTicketInOrg(ticketId, user.organizationId());

        TicketComment comment = new TicketComment();
        comment.setTicketId(ticketId);
        comment.setAuthorId(user.userId());
        comment.setContent(request.getContent());
        comment.setOrganizationId(user.organizationId());
        TicketComment saved = ticketCommentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}