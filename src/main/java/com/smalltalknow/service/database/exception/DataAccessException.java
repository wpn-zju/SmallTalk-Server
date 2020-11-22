package com.smalltalknow.service.database.exception;

public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
}
