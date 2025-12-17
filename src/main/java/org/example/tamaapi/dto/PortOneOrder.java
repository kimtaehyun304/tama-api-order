package org.example.tamaapi.dto;

import lombok.*;
import org.example.tamaapi.dto.requestDto.order.PortOneOrderItem;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class PortOneOrder {

    private String paymentId;

    private String senderNickname;

    private String senderEmail;

    //---받는 고객---
    private String receiverNickname;

    private String receiverPhone;

    // 우편번호
    private String zipCode;

    private String streetAddress;

    // 상세 주소
    private String detailAddress;

    private String deliveryMessage;

    private Long memberCouponId;

    private int usedPoint;

    private List<PortOneOrderItem> orderItems = new ArrayList<>();
}
