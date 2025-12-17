package org.example.tamaapi.common.exception;

public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException() {
        super("재고가 부족합니다");
    }
}
