package com.jubeiwato.costing_service.entities;

import com.jubeiwato.costing_service.constants.PartType;

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
@Table(name = "part", schema = "app")
public class Part extends BaseEntity{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
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
        private String unit;
}
