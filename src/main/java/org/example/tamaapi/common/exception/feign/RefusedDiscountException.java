package org.example.tamaapi.common.exception.feign;

public class RefusedDiscountException extends CustomFeignException {
    public RefusedDiscountException(String message) {
        super("REFUSED_DISCOUNT", message);
    }
}
