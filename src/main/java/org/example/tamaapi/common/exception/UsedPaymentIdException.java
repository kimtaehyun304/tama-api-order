package org.example.tamaapi.common.exception;

public class UsedPaymentIdException extends RuntimeException{

    //결제가 순간 두번 되거나, 악의적으로 결제번호를 재사용한 경우
    public UsedPaymentIdException() {
        super("이미 결제됐습니다.");
    }
}
