package com.SmartManagementSystem.SMMS.repository;


import com.SmartManagementSystem.SMMS.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


public interface TicketRepository extends JpaRepository<Ticket,Long> {

    List<Ticket> findByOrganizationId(Long organizationId);
    List<Ticket> findByOrganizationIdAndCreatedBy(Long organizationId, Long createdBy);
    Optional<Ticket> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("SELECT t FROM Ticket t WHERE t.organizationId = :orgId " +
            "AND (:createdBy IS NULL OR t.createdBy = :createdBy) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "AND (CAST(:search AS string) IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    List<Ticket> search(@Param("orgId") Long orgId,
                        @Param("createdBy") Long createdBy,
                        @Param("status") String status,
                        @Param("priority") String priority,
                        @Param("category") String category,
                        @Param("search") String search);
}
