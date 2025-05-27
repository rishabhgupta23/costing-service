package com.jubeiwato.costing_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "part_file", schema = "app",
       uniqueConstraints = @UniqueConstraint(columnNames = {"part_id", "company_id", "file_url"}))
public class PartFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;
    
    @ManyToOne
    @JoinColumn(name = "part_id", referencedColumnName = "part_id", nullable = false)
    private Part part;
    
    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)    
    private Company company;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;
}
