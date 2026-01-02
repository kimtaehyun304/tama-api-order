package org.example.tamaapi.common.exception.feign;

public class MemberFeignQueryException extends RuntimeException{
    public MemberFeignQueryException(String message) {
        super(message);
    }
}
