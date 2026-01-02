package org.example.tamaapi.common.exception.feign;

public class ItemFeignQueryException extends RuntimeException{
    public ItemFeignQueryException(String message) {
        super(message);
    }
}
