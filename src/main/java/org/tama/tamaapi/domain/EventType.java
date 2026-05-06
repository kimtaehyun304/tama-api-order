package org.tama.tamaapi.domain;

import lombok.Getter;

@Getter
public enum EventType {
    ORDER_RECEIVED("주문 접수"),
    IN_DELIVERY("배송 중"),
    DELIVERED("배송 완료"),
    COMPLETED("구매 확정"),
    CANCEL_RECEIVED("취소 접수"),
    IN_RETURN("반품 중"),
    RETURNED("반품 완료"),
    IN_REFUND("환불 중"),
    REFUNDED("환불 완료"),
    PG_CANCEL_ERROR("결제 취소 중"), //현재 PG 서버 장애로 인해, 나중에 스케줄러를 통해 취소 재요청

    //-----

    ROLLBACK_COUPON_AND_POINT("");

    private final String kor;

    EventType(String kor) {
        this.kor = kor;
    }
}
