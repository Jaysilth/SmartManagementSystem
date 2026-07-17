package com.SmartManagementSystem.SMMS.repository;


import com.SmartManagementSystem.SMMS.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface TicketRepository extends JpaRepository<Ticket,Long> {

    List<Ticket> findByOrganizationId(Long organizationId);
    List<Ticket> findByOrganizationIdAndCreatedBy(Long organizationId, Long createdBy);

}
