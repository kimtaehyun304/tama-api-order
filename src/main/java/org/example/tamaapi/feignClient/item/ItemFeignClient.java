package org.example.tamaapi.feignClient.item;

import org.example.tamaapi.common.exception.feign.ItemFeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//k8s로 바꾸면서 url 옵션 생략
@FeignClient(name = "item-service"
        , configuration = ItemFeignClientConfig.class
        , fallbackFactory = ItemFallbackFactory.class)
public interface ItemFeignClient {

    @GetMapping("/api/items/totalPrice")
    int getTotalPrice(@RequestBody List<ItemOrderCountRequest> requests);

    @GetMapping("/api/items/price")
    List<ItemPriceResponse> getItemsPrice(@RequestParam List<Long> colorItemSizeStockIds);

    @PutMapping("/api/items/stocks/increase")
    void increaseStocks(@RequestBody List<ItemOrderCountRequest> requests);

    //+테이블에 감소 로그 저장
    @PutMapping("/api/items/stocks/decrease")
    void decreaseStocks(@RequestBody List<ItemOrderCountRequest> requests, @RequestParam String uuid);

    @GetMapping("/api/items/stock/decrease/log/exist")
    boolean existDecreaseStockLog(@RequestParam String paymentId);

}
