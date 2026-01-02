package org.example.tamaapi.common.exception.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ItemFeignClientConfig  {
    private final ObjectMapper objectMapper;
    @Bean(name = "itemErrorDecoder")
    public ErrorDecoder errorDecoder() {
        return new ItemFeignErrorDecoder(objectMapper);
    }
}
