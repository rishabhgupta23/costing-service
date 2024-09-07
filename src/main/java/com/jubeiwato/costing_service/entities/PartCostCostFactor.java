package com.jubeiwato.costing_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "part_cost_cost_factor")
public class PartCostCostFactor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "part_cost_cost_factor_id")
    private Long partCostCostFactorId;

    @ManyToOne
    @JoinColumn(name = "part_cost_id", referencedColumnName = "part_cost_id")
    private PartCost partCost;

    @OneToOne
    @JoinColumn(name = "factor_id", referencedColumnName = "factor_id")
    private CostFactor costFactor;

    private double value;
}
