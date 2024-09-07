package com.jubeiwato.costing_service.constants;

import lombok.Getter;

@Getter
public enum DeleteFlag {
    POSTITVE(1),
    NEGATIVE(0);


    private Integer value;
    
    DeleteFlag(Integer value) {
        this.value = value;
    }
}
