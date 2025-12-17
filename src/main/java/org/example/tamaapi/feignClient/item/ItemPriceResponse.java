package org.example.tamaapi.feignClient.item;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPriceResponse {

    @NotNull
    private Long colorItemSizeStockId;

    @NotNull
    private int price;

}
