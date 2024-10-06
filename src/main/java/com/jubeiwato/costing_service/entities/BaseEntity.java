package com.jubeiwato.costing_service.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity {
    @Column(name = "created_by")
    Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_date_time", updatable = false)
    LocalDateTime createdDateTime;
    
    @Column(name = "updated_by")
    Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_date_time")
    LocalDateTime updatedDateTime;

    @Column(name = "delete_flag", columnDefinition = "integer default 0")
    Integer deleteFlag;


    @PrePersist
    public void prePersist() {
        if(deleteFlag == null) {
            deleteFlag = 0;
        }
    }
}
