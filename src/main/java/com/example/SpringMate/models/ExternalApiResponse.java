package com.example.SpringMate.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalApiResponse {

    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("data")
    private Data data;

    private static class Data{

        @JsonProperty("color")
        private String color;
        @JsonProperty("capacity")
        private String capacity;
        @JsonProperty("price")
        private String price;
    }
}
