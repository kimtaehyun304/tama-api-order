package org.example.tamaapi.domain.order;

public enum OrderStatus {
    IN_PEND("주문 중"),
    ORDER_RECEIVED("주문 접수"),
    IN_DELIVERY("배송 중"),
    DELIVERED("배송 완료"),
    COMPLETED("구매 확정"),
    CANCEL_RECEIVED("취소 접수"),
    IN_RETURN("반품 중"),
    RETURNED("반품 완료"),
    IN_REFUND("환불 중"),
    REFUNDED("환불 완료");

    private final String kor;

    OrderStatus(String kor) {
        this.kor = kor;
    }

}
