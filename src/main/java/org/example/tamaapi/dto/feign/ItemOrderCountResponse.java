package org.example.tamaapi.dto.feign;

import lombok.Getter;
import org.example.tamaapi.domain.order.OrderItem;

@Getter
public class ItemOrderCountResponse {

    private Long colorItemSizeStockId;

    private int count;

    public ItemOrderCountResponse(OrderItem orderItem) {
        this.colorItemSizeStockId = orderItem.getColorItemSizeStockId();
        this.count = orderItem.getCount();
    }
}
