package org.example.tamaapi.feignClient.item;

import org.example.tamaapi.common.exception.feign.ItemFeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "item-service", url = "http://localhost:5002"
        , configuration = ItemFeignClientConfig.class
        , fallbackFactory = ItemFallbackFactory.class)
public interface ItemFeignClient {

    @GetMapping("/api/items/totalPrice")
    int getTotalPrice(@RequestBody List<ItemOrderCountRequest> requests);

    @GetMapping("/api/items/price")
    List<ItemPriceResponse> getItemsPrice(@RequestParam List<Long> colorItemSizeStockIds);

    @PutMapping("/api/items/stocks/increase")
    void increaseStocks(@RequestBody List<ItemOrderCountRequest> requests);

    @PutMapping("/api/items/stocks/decrease")
    void decreaseStocks(@RequestBody List<ItemOrderCountRequest> requests);

}
