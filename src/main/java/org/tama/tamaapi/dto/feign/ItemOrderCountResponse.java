package org.tama.tamaapi.dto.feign;

import lombok.Getter;
import org.tama.tamaapi.domain.order.OrderItem;

@Getter
public class ItemOrderCountResponse {

    private Long colorItemSizeStockId;

    private int count;

    public ItemOrderCountResponse(OrderItem orderItem) {
        this.colorItemSizeStockId = orderItem.getColorItemSizeStockId();
        this.count = orderItem.getCount();
    }
}
