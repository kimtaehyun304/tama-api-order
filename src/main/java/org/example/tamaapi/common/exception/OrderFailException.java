package org.example.tamaapi.common.exception;

import lombok.Getter;

@Getter
public class OrderFailException extends RuntimeException {


    public OrderFailException(String message) {
        super(String.format("주문을 실패했습니다. 이유:%s", message));
    }



}
