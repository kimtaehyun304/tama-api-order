package org.example.tamaapi.common.exception.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MemberFeignClientConfig {

    private final ObjectMapper objectMapper;
    
    @Bean(name = "memberErrorDecoder")
    public ErrorDecoder errorDecoder() {
        return new MemberFeignErrorDecoder(objectMapper);
    }
}
