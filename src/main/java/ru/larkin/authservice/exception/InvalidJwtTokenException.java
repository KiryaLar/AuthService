package ru.larkin.authservice.exception;

public class InvalidJwtTokenException extends AuthAppException {
    public InvalidJwtTokenException(String message) {
        super(ExceptionType.INVALID_TOKEN_EXCEPTION, message);
    }
}
