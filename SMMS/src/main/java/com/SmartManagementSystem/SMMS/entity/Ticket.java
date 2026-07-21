package com.SmartManagementSystem.SMMS.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "assigned_technician_id")
    private Long assignedTechnicianId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String priority = "MEDIUM";
}
