package org.tama.tamaapi.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.tama.tamaapi.feignClient.item.ItemOrderCountRequest;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IncreaseStockEvent {

    //또다른 주문 관련한 상품 이벤트 발행 계획은 없어서 ItemEvent로 안 함

    private String paymentId;
    private List<ItemOrderCountRequest> requests;

}
