package com.example.ToDo.Exception;

public class UserAlreadyFoundException extends Exception{
    public UserAlreadyFoundException(String message) {
        super(message);
    }
}
