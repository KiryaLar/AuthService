package ru.larkin.authservice.exception;

import lombok.Getter;

@Getter
public class AuthAppException extends RuntimeException{

    private final ExceptionType exceptionType;

    public AuthAppException(ExceptionType exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }
}
