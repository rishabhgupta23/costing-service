package com.jubeiwato.costing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "part_cost_cost_factor",
        schema = "app"
)
public class PartCostCostFactor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_cost_cost_factor_id")
    private Long partCostCostFactorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "part_cost_id",
            nullable = false
    )
    private PartCost partCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "factor_id",
            referencedColumnName = "factor_id",
            nullable = false
    )
    private CostFactor costFactor;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "value", nullable = false)
    private double value;

    @Column(name = "comments", length = 255)
    private String comments;
}