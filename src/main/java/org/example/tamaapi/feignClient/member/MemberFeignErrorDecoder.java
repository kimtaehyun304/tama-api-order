package org.example.tamaapi.feignClient.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.common.dto.SimpleFeignResponse;
import org.example.tamaapi.exception.RefusedDiscountException;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
public class MemberFeignErrorDecoder implements ErrorDecoder {

    //private final ErrorDecoder defaultDecoder = new Default();

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = readBody(response);
        SimpleFeignResponse feignRes = serializeBody(body);

        if(feignRes.getCode().equals("REFUSED_DISCOUNT"))
            return new RefusedDiscountException(feignRes.getMessage());

        return new RuntimeException(feignRes.getMessage());
    }

    private String readBody(Response response){
        try {
            return Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        } catch (Exception e){
            throw new RuntimeException("메시지 바디 read 실패");
        }
    }

    private SimpleFeignResponse serializeBody(String body){
        try {
            return objectMapper.readValue(body, SimpleFeignResponse.class);
        } catch (Exception e){
            // 이미 실패한 상황이라 serializeBody 까지 끝나야 원인 알수 있음
            throw new RuntimeException("메시지 바디 직렬화 실패");
        }
    }

}
