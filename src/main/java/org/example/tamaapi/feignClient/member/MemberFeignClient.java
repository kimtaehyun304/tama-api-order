package org.example.tamaapi.feignClient.member;

import org.example.tamaapi.common.exception.feign.MemberFeignClientConfig;
import org.example.tamaapi.dto.feign.UsedCouponAndPointRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//k8s로 바꾸면서 url 옵션 생략
@FeignClient(name = "member-service"
        , configuration = MemberFeignClientConfig.class
        , fallbackFactory = MemberFallbackFactory.class)
public interface MemberFeignClient {

    @GetMapping("/api/member/coupon/{memberCouponId}/price")
    int getCouponPrice(
            @PathVariable("memberCouponId") Long memberCouponId,
            @RequestParam("orderItemsPrice") int orderItemsPrice
    );

    //+쿠폰,포인트 사용 로그 남김
    @PutMapping("/api/member/discount/use")
    void useCouponAndPoint(@RequestBody UsedCouponAndPointRequest usedCouponAndPointRequest);

    /* 나중에 환불 접수된 주문 처리 기능 만들때 필요
    @PutMapping("/api/member/discount/rollback")
    void rollbackCouponAndPoint(@RequestBody UsedCouponAndPointRequest usedCouponAndPointRequest, @RequestHeader("Authorization") String bearerJwt);
     */

    @GetMapping("/api/member/{memberId}/authority")
    Authority findAuthority(@PathVariable Long memberId);

    @GetMapping("/api/member/coupon/{memberCouponId}/validate")
    void validateCoupon(@PathVariable Long memberCouponId);

    @GetMapping("/api/member/discount/log/exist")
    boolean existDiscountLog(@RequestParam String paymentId);
}
