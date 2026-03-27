package org.example.tamaapi.common.exception.feign;

public class NotEnoughStockException extends CustomFeignException {

    //메시지는 회원 서버에서 바꿀 가능성 있어서
    public NotEnoughStockException(String message) {
        super("NOT_ENOUGH_STOCK", message);
    }
}
