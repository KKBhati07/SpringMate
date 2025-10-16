package com.example.SpringMate.Shared.Exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(){
        super("Requested Resource not found");
    }
    public NotFoundException(String msg){
        super(msg);
    }

}
