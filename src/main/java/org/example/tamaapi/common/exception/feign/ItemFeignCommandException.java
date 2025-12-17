package org.example.tamaapi.common.exception.feign;

public class ItemFeignCommandException extends RuntimeException{
    public ItemFeignCommandException(String message) {
        super(message);
    }
}
