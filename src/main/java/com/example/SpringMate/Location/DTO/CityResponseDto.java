package com.example.SpringMate.Location.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CityResponseDto {
    private Long id;
    private String name;
}
