package org.example.tamaapi.feignClient.item;

import org.springframework.stereotype.Component;

import java.util.List;

import static org.example.tamaapi.common.exception.CommonExceptionHandler.throwOriginalException;


public class ItemFallback implements ItemFeignClient{

    private final Throwable cause;

    public ItemFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public int getTotalPrice(List<ItemOrderCountRequest> requests) {
        throwOriginalException(cause);
        return 0;
    }

    @Override
    public List<ItemPriceResponse> getItemsPrice(List<Long> colorItemSizeStockIds) {
        throwOriginalException(cause);
        return null;
    }

    @Override
    public void increaseStocks(List<ItemOrderCountRequest> requests) {
        throwOriginalException(cause);
    }

    @Override
    public void decreaseStocks(List<ItemOrderCountRequest> requests) {
        throwOriginalException(cause);
    }

}
