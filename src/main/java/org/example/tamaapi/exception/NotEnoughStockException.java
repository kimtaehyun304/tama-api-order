package org.example.tamaapi.exception;

import org.example.tamaapi.common.exception.CustomFeignException;

public class NotEnoughStockException extends CustomFeignException {

    //메시지는 회원 서버에서 바꿀 가능성 있어서
    public NotEnoughStockException(String message) {
        super("NOT_ENOUGH_STOCK", message);
    }
}
