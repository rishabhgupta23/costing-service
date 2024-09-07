package com.jubeiwato.costing_service.entities;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "parts")
public class Part extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "part_id")
        private Long partId;

        @Column(name = "part_name")
        private String partName;

        @Column(name = "part_number", unique = true)
        private String partNumber;

        @Column(name = "category_name")
        private String categoryName;

        @Column(name = "type")
        @Enumerated(EnumType.STRING)
        private PartType type;

        @Column(name = "unit")
        @Enumerated(EnumType.STRING)
        private PartUnit unit;
}
