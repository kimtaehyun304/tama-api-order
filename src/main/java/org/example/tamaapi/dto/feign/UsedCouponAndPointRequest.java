package org.example.tamaapi.dto.feign;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsedCouponAndPointRequest {

    private Long memberCouponId;

    @PositiveOrZero
    private int usedCouponPrice;

    @PositiveOrZero
    private int usedPoint;

    @PositiveOrZero
    private int rewardPoint;

    @Positive
    private int orderItemsPrice;

}
