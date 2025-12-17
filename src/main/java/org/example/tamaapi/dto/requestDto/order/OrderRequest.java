package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class OrderRequest {

    //무료 주문은 PG사를 안거치므로 누락되도 정상
    private String paymentId;

    //--주문 고객---
    //엑세스 토큰이 있는 경우 누락되도 정상
    private String senderNickname;

    //엑세스 토큰이 있는 경우 누락되도 정상
    private String senderEmail;

    //---받는 고객---
    @NotBlank
    private String receiverNickname;

    @NotBlank
    private String receiverPhone;

    // 우편번호
    @NotBlank
    private String zipCode;

    @NotBlank
    private String streetAddress;

    // 상세 주소
    @NotBlank
    private String detailAddress;

    @NotBlank
    private String deliveryMessage;

    private Long memberCouponId;

    private int usedPoint;

    @NotEmpty
    private List<PortOneOrderItem> orderItems = new ArrayList<>();
}
