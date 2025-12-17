package org.example.tamaapi.common.exception.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemberFeignClientConfig {

    @Bean(name = "memberErrorDecoder")
    public ErrorDecoder errorDecoder() {
        return new MemberFeignErrorDecoder();
    }
}
