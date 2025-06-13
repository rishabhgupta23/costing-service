package com.jubeiwato.costing_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Entity
@Table(name = "part_part_attribute", schema = "app",
    uniqueConstraints = @UniqueConstraint(columnNames = {"part_id", "attribute_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartPartAttribute extends BaseEntity {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_attribute_id")
    private Long partPartAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false, referencedColumnName = "part_id")
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false, referencedColumnName = "attribute_id")
    private PartAttribute attribute;

    @Column(name = "attribute_value")
    private String attributeValue;
}