package com.jubeiwato.costing_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "cost_factor_company_name_delete_flag_unique",
        columnNames = {"company_id", "factor_name", "delete_flag"}
    )
)
public class CostFactor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factor_id")
    private long factorId;

    @Column(name = "factor_name")
    private String factorName;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
    private Company company;
}
