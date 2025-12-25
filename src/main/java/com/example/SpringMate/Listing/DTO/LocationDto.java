package com.example.SpringMate.Listing.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LocationDto {

    private Long id;
    private CityDto city;
    private StateDto state;
    private CountryDto country;


    @Data
    @Builder
    @AllArgsConstructor
    public static class CityDto {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class StateDto {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CountryDto {
        private Long id;
        private String name;
    }
}
