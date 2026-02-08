package com.example.SpringMate.Listing.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ConditionDto {

    private Long id;
    private String code;
    private String label;
    private String description;
    private Integer sortOrder;

}
