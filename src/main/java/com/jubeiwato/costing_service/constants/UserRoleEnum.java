package com.jubeiwato.costing_service.constants;

public enum UserRoleEnum {
    ADMIN("ADMIN"),
    SUPER_ADMIN("SUPER_ADMIN"),
    MAINTAINER("MAINTAINER"),
    GUEST("GUEST");

    private final String roleName;

    UserRoleEnum(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
