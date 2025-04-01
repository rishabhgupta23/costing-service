package com.jubeiwato.costing_service.entities;

import com.jubeiwato.costing_service.constants.PartType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "part",
    schema = "app",
    uniqueConstraints = @UniqueConstraint(
    name = "part_company_part_number_unique",
    columnNames = {"company_id", "part_number"}
    )
)
public class Part extends BaseEntity{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "part_id")
        private Long partId;

        @Column(name = "part_name")
        private String partName;

        @Column(name = "part_number")
        private String partNumber;

        @Column(name = "category_name")
        private String categoryName;

        @Column(name = "type")
        @Enumerated(EnumType.STRING)
        private PartType type;

        @Column(name = "unit")
        private String unit;

        @ManyToOne
        @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
        private Company company;

        @OneToMany(mappedBy = "part", fetch = FetchType.LAZY)
        private List<PartCost> partCosts;
}
