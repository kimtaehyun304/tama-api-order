package org.example.tamaapi.common.exception;

public class MyInternalServerException extends RuntimeException {
    public MyInternalServerException(String message) {
        super(message);
    }
}
