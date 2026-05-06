package org.tama.tamaapi.feignClient.item;

import org.tama.tamaapi.exception.CommonExceptionHandler;

import java.util.List;


public class ItemFallback implements ItemFeignClient{

    private final Throwable cause;

    public ItemFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public int getTotalPrice(List<ItemOrderCountRequest> requests) {
        CommonExceptionHandler.throwOriginalException(cause);
        return 0;
    }

    @Override
    public List<ItemPriceResponse> getItemsPrice(List<Long> colorItemSizeStockIds) {
        CommonExceptionHandler.throwOriginalException(cause);
        return null;
    }

    @Override
    public void increaseStocks(List<ItemOrderCountRequest> requests) {
        CommonExceptionHandler.throwOriginalException(cause);
    }

    @Override
    public void decreaseStocks(List<ItemOrderCountRequest> requests, String uuid) {
        CommonExceptionHandler.throwOriginalException(cause);
    }

    @Override
    public boolean existDecreaseStockLog(String paymentId) {
        CommonExceptionHandler.throwOriginalException(cause);
        //어짜피 예외발생해서 그냥 false로 해둠. Boolean 타입이 아니라 null 불가
        return false;
    }

}
