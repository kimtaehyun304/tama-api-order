package org.example.tamaapi.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.EventType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RollbackCouponAndPointEvent {

    private String paymentId;
    private Long memberId;
    private Long memberCouponId;
    private Integer usedPoint;
    private Integer rewardPoint;

}
