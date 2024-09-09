package com.jubeiwato.costing_service.entities;

import com.jubeiwato.costing_service.entities.ids.BomId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
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
@IdClass(BomId.class)
@Table(name = "bom")
public class Bom extends BaseEntity {
    @Id
    @OneToOne
    @JoinColumn(name = "parent_part_id", referencedColumnName = "part_id")
    private Part parentPart;
    
    @Id
    @OneToOne
    @JoinColumn(name = "part_id", referencedColumnName = "part_id")
    private Part childPart;

    private Double quantity;
}
