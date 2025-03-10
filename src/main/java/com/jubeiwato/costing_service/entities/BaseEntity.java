package com.jubeiwato.costing_service.entities;

import java.time.LocalDateTime;

import jakarta.persistence.EntityListeners;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @CreatedBy
    @Column(name = "created_by")
    Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_date_time", updatable = false)
    LocalDateTime createdDateTime;

    @LastModifiedBy
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
