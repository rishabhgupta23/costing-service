package com.jubeiwato.costing_service.entities;

import com.jubeiwato.costing_service.constants.CostFactorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "cost_factor",
        schema = "app",
        uniqueConstraints = @UniqueConstraint(
                name = "cost_factor_company_name_unique",
                columnNames = {"factor_name", "company_id"}
        )
)
public class CostFactor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factor_id")
    private Long factorId;

    @Column(name = "factor_name", nullable = false, length = 50)
    private String factorName;

    @ManyToOne
    @JoinColumn(
            name = "company_id",
            referencedColumnName = "company_id",
            nullable = false
    )
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "factor_type", nullable = false, length = 20)
    private CostFactorType factorType;
}