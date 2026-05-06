package org.tama.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class FreeOrderRequest {

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
