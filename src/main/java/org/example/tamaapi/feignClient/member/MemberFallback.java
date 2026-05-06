package org.example.tamaapi.feignClient.member;

import org.example.tamaapi.dto.feign.UsedCouponAndPointRequest;

import static org.example.tamaapi.exception.CommonExceptionHandler.throwOriginalException;

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
    public void useCouponAndPoint(UsedCouponAndPointRequest usedCouponAndPointRequest) {
        throwOriginalException(cause);
    }

    @Override
    public Authority findAuthority(Long memberId) {
        throwOriginalException(cause);
        return null;
    }

    @Override
    public void validateCoupon(Long memberCouponId) {
        throwOriginalException(cause);
    }

    @Override
    public boolean existDiscountLog(String paymentId) {
        throwOriginalException(cause);
        return false;
    }

}
