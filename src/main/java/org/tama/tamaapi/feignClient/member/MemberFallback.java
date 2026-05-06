package org.tama.tamaapi.feignClient.member;

import org.tama.tamaapi.dto.feign.UsedCouponAndPointRequest;
import org.tama.tamaapi.exception.CommonExceptionHandler;

public class MemberFallback implements MemberFeignClient{

    private final Throwable cause;

    public MemberFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public int getCouponPrice(Long memberCouponId, int orderItemsPrice) {
        CommonExceptionHandler.throwOriginalException(cause);
        return 0;
    }

    @Override
    public void useCouponAndPoint(UsedCouponAndPointRequest usedCouponAndPointRequest) {
        CommonExceptionHandler.throwOriginalException(cause);
    }

    @Override
    public Authority findAuthority(Long memberId) {
        CommonExceptionHandler.throwOriginalException(cause);
        return null;
    }

    @Override
    public void validateCoupon(Long memberCouponId) {
        CommonExceptionHandler.throwOriginalException(cause);
    }

    @Override
    public boolean existDiscountLog(String paymentId) {
        CommonExceptionHandler.throwOriginalException(cause);
        return false;
    }

}
