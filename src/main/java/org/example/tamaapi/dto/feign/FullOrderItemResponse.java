package org.example.tamaapi.dto.feign;

import lombok.*;
import org.example.tamaapi.domain.order.OrderItem;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Setter
public class FullOrderItemResponse {

    private Long id;

    private Long orderId;

    private Long colorItemSizeStockId;

    //구매 후 가격이 바뀔 수 있어서 당시 가격 남겨야함 (할인을 시작하거나, 할인이 끝나거나)
    private int orderPrice;

    private int count;

    public FullOrderItemResponse(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.orderId = orderItem.getOrder().getId();
        this.colorItemSizeStockId = orderItem.getColorItemSizeStockId();
        this.orderPrice = orderItem.getOrderPrice();
        this.count = orderItem.getCount();
    }
}
