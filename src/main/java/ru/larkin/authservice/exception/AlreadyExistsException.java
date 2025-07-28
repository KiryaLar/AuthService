package ru.larkin.authservice.exception;

public class AlreadyExistsException extends AuthAppException {
    public AlreadyExistsException(String message) {
        super(ExceptionType.BAD_REQUEST_EXCEPTION, message);
    }
}
