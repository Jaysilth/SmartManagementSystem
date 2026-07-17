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

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "organization_id")
    private Long organizationId;
}
