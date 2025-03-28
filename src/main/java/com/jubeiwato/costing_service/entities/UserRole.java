package com.jubeiwato.costing_service.entities;

import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "user_role", schema = "app")
public class UserRole extends BaseEntity implements GrantedAuthority {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(name = "role_id")
      private Long roleId;

      @Column(name = "role_name")
      private String roleName;

      @Override
      public String getAuthority() {
            return "ROLE_" + roleName.toUpperCase().replace(" ", "_");
            // return "ROLE_" + roleName.toUpperCase();
      }
}