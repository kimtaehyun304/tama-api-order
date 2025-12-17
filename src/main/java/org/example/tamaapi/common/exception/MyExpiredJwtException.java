package org.example.tamaapi.common.exception;

public class MyExpiredJwtException extends RuntimeException {
    public MyExpiredJwtException(String message){ super(message);}
}
