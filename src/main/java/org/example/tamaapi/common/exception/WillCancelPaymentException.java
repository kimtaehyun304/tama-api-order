package org.example.tamaapi.common.exception;

public class WillCancelPaymentException extends RuntimeException{

    public WillCancelPaymentException(String reason) {
        super(String.format("주문을 실패했습니다. 결제는 취소될 예정입니다. 이유: %s", reason));
    }
}
