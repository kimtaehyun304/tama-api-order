package org.tama.tamaapi.dto.requestDto.order;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class OrderDetailRequest {

   private Long memberId;
   private int orderItemsPrice;
   private int clientTotal;
   private int usedCouponPrice;
}
