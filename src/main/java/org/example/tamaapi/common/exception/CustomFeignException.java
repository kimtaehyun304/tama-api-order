package org.example.tamaapi.common.exception;

import lombok.Getter;

@Getter
public class CustomFeignException extends RuntimeException {

    private final String code;

    public CustomFeignException(String code, String message) {
        super(message);
        this.code = code;
    }
}
