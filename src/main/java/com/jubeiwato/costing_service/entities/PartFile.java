package com.jubeiwato.costing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "part_file", schema = "app",
    uniqueConstraints = @UniqueConstraint(columnNames = {"part_id", "s3_file_key"})
)
public class PartFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_file_id")
    private Long partfileId;

    @ManyToOne
    @JoinColumn(name = "part_id", referencedColumnName = "part_id", nullable = false)
    private Part part;

    @Column(name = "s3_file_key", nullable = false)
    private String s3FileKey;
}