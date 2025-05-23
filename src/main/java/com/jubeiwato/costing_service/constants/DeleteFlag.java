package com.jubeiwato.costing_service.constants;

import lombok.Getter;

@Getter
public enum DeleteFlag {
    POSITIVE(1),
    NEGATIVE(0);

    private Integer value;

    DeleteFlag(Integer value) {
        this.value = value;
    }
}
