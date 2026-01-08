package org.example.tamaapi.feignClient.member;

import org.example.tamaapi.dto.feign.UsedCouponAndPointRequest;
import org.springframework.stereotype.Component;

import static org.example.tamaapi.common.exception.CommonExceptionHandler.throwOriginalException;

public class MemberFallback implements MemberFeignClient{

    private final Throwable cause;

    public MemberFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public int getCouponPrice(Long memberCouponId, int orderItemsPrice) {
        throwOriginalException(cause);
        return 0;
    }

    @Override
    public void useCouponAndPoint(UsedCouponAndPointRequest usedCouponAndPointRequest, String bearerJwt) {
        throwOriginalException(cause);
    }

    @Override
    public Authority findAuthority(String bearerJwt) {
        throwOriginalException(cause);
        return null;
    }

    @Override
    public void validateCoupon(Long memberCouponId) {
        throwOriginalException(cause);
    }

}
