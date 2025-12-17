package org.example.tamaapi.common.exception.feign;

public class MemberFeignCommandException extends RuntimeException{
    public MemberFeignCommandException(String message) {
        super(message);
    }
}
