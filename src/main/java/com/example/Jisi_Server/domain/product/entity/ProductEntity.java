package com.example.Jisi_Server.domain.product.entity;

import com.example.Jisi_Server.domain.company.entity.CompanyEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "product")
@Getter @Setter
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer price;

    private Integer sellCount;

    @ManyToOne
    @JoinColumn(name = "company", referencedColumnName = "id", nullable = false)
    private CompanyEntity company;

    @Column(columnDefinition = "TEXT")
    private String description;
}
