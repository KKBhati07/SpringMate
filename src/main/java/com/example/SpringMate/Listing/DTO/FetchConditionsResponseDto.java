package com.example.SpringMate.Listing.DTO;

import com.example.SpringMate.Listing.Entity.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FetchConditionsResponseDto {
    List<Condition> conditions;
}
