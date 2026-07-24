package com.SmartManagementSystem.SMMS.repository;

import com.SmartManagementSystem.SMMS.entity.WorkNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkNoteRepository extends JpaRepository<WorkNote, Long> {
    List<WorkNote> findByTicketIdAndOrganizationIdOrderByCreatedAtAsc(Long ticketId, Long organizationId);
}