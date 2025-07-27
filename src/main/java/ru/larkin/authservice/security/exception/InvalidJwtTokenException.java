package ru.larkin.authservice.security.exception;

public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException(String invalidTokenType) {
    }
}
