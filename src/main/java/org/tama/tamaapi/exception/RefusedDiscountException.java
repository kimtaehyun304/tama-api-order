package org.tama.tamaapi.exception;


import org.tama.sharelib.common.exception.CustomFeignException;

public class RefusedDiscountException extends CustomFeignException {
    public RefusedDiscountException(String message) {
        super("REFUSED_DISCOUNT", message);
    }
}
