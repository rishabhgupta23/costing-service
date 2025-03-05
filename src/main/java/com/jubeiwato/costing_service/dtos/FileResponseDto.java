package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    String fileData;
    String fileName;
}
