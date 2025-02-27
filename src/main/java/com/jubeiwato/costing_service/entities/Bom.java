package com.jubeiwato.costing_service.entities;

import com.jubeiwato.costing_service.entities.ids.BomId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(BomId.class)
@Table(name = "bom", schema = "app")
public class Bom extends BaseEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "parent_part_id", referencedColumnName = "part_id")
    private Part parentPart;
    
    @Id
    @ManyToOne
    @JoinColumn(name = "part_id", referencedColumnName = "part_id")
    private Part childPart;

    private Double quantity;
}
