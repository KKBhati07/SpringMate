package com.example.SpringMate.Util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Response<T> {
    private T data;
    private String message;

    public Response(String message){
        this.message = message;
    }
}
