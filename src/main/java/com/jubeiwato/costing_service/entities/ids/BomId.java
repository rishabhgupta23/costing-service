package com.jubeiwato.costing_service.entities.ids;

import java.io.Serializable;

import com.jubeiwato.costing_service.entities.Part;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BomId implements Serializable {
    private Part parentPart;
    private Part childPart;
}
