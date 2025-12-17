package org.example.tamaapi.common.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("인증되지 않았습니다.");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
