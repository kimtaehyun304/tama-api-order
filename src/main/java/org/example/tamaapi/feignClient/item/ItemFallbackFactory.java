package org.example.tamaapi.feignClient.item;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;


@Component
public class ItemFallbackFactory implements FallbackFactory<ItemFallback> {

    @Override
    public ItemFallback create(Throwable cause) {
        return new ItemFallback(cause);
    }
}
