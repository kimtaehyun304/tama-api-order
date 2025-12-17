package org.example.tamaapi.feignClient.item;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.dto.requestDto.order.PortOneOrderItem;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOrderCountRequest {

    @NotNull
    private Long colorItemSizeStockId;

    @NotNull
    private Integer orderCount;

    public ItemOrderCountRequest(PortOneOrderItem portOneOrderItem) {
        this.colorItemSizeStockId = portOneOrderItem.getColorItemSizeStockId();
        this.orderCount = portOneOrderItem.getOrderCount();
    }

}
