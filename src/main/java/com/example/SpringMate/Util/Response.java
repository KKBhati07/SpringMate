package com.example.SpringMate.Util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
    private Object data;
    private String message;
}
