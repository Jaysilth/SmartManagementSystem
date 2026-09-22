package com.SmartManagementSystem.SMMS.repository;

import com.SmartManagementSystem.SMMS.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    List<TicketAttachment> findByTicketIdAndOrganizationId(Long ticketId, Long organizationId);
}