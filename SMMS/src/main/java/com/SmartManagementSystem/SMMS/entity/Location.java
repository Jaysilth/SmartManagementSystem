package com.SmartManagementSystem.SMMS.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "parent_location_id")
    private Long parentLocationId;

    @Column(name = "organization_id")
    private Long organizationId;
}