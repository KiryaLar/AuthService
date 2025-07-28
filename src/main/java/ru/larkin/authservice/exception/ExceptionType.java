package ru.larkin.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionType {
  BAD_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST.value()),
  INVALID_TOKEN_EXCEPTION(HttpStatus.BAD_REQUEST.value());

  private final int statusCode;

  ExceptionType(int statusCode) {
    this.statusCode = statusCode;
  }
}
