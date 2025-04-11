package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleDto {
    private Long roleId;
    private String roleName;

    
    public static UserRoleDto entityToDto(UserRole role) {
        return UserRoleDto.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .build();
    }
}