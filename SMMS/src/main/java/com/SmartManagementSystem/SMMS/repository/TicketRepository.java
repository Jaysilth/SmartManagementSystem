package com.SmartManagementSystem.SMMS.repository;


import com.SmartManagementSystem.SMMS.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface TicketRepository extends JpaRepository<Ticket,Long> {

    List<Ticket> findByOrganizationId(Long organizationId);
    List<Ticket> findByOrganizationIdAndCreatedBy(Long organizationId, Long createdBy);
    Optional<Ticket> findByIdAndOrganizationId(Long id, Long organizationId);

}
