package org.example.tamaapi.exception;

import org.example.tamaapi.common.exception.CustomFeignException;

public class RefusedDiscountException extends CustomFeignException {
    public RefusedDiscountException(String message) {
        super("REFUSED_DISCOUNT", message);
    }
}
