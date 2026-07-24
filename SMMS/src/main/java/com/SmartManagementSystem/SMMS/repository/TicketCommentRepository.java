package com.SmartManagementSystem.SMMS.repository;

import com.SmartManagementSystem.SMMS.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdAndOrganizationIdOrderByCreatedAtAsc(Long ticketId, Long organizationId);
}