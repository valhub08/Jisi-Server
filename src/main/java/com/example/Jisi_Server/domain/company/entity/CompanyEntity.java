package com.example.Jisi_Server.domain.company.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "company")
public class CompanyEntity {
    @Id
    private Long id;
}
