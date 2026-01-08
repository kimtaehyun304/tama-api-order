package org.example.tamaapi.feignClient.member;

import org.example.tamaapi.feignClient.item.ItemFallback;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;


@Component
public class MemberFallbackFactory implements FallbackFactory<MemberFallback> {

    @Override
    public MemberFallback create(Throwable cause) {
        return new MemberFallback(cause);
    }

}
