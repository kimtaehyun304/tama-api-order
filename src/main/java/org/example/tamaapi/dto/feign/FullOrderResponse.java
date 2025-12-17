package org.example.tamaapi.dto.feign;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.order.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
public class FullOrderResponse {

    private Long id;

    private Long memberId;

    private Delivery delivery;

    private OrderStatus status;

    private Guest guest;

    private Long memberCouponId;

    private int usedCouponPrice;

    private int usedPoint;

    private int shippingFee;

    private String paymentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<FullOrderItemResponse> orderItems;

    public FullOrderResponse(Order order) {
        this.id = order.getId();
        this.memberId = order.getMemberId();
        this.delivery = order.getDelivery();
        this.status = order.getStatus();
        this.guest = order.getGuest();
        this.memberCouponId = order.getMemberCouponId();
        this.usedCouponPrice = order.getUsedCouponPrice();
        this.usedPoint = order.getUsedPoint();
        this.shippingFee = order.getShippingFee();
        this.paymentId = order.getPaymentId();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        List<FullOrderItemResponse> orderItems = order.getOrderItems().stream().map(FullOrderItemResponse::new).toList();
        this.orderItems = orderItems;
    }

}
