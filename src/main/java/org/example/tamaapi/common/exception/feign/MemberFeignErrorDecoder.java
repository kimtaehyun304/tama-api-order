package org.example.tamaapi.common.exception.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.springframework.stereotype.Component;
import org.springframework.validation.SimpleErrors;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
public class MemberFeignErrorDecoder implements ErrorDecoder {

    //private final ErrorDecoder defaultDecoder = new Default();

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        String httpMethod = response.request().httpMethod().name();
        String body = readBody(response);
        String message = serializeBody(body);

        // PUT, POST, DELETE일 때만 우리 custom exception
        if (httpMethod.equals("PUT") || httpMethod.equals("POST") || httpMethod.equals("DELETE"))
            return new MemberFeignCommandException(message);

        // 나머지(GET 등)은 기본 예외 처리
        return new MemberFeignQueryException(message);
    }

    private String readBody(Response response){
        try {
            return Util.toString(
                    response.body().asReader(StandardCharsets.UTF_8));
        } catch (Exception e){
            log.error("메시지 바디 read 실패");
            // 이미 실패한 상황이라 serializeBody 까지 끝나야 원인 알수 있음
            return "서버에서 오류가 발생했습니다.";
            //return e.getMessage();
        }
    }

    private String serializeBody(String body){
        try {
            SimpleResponse simpleResponse = objectMapper.readValue(body, SimpleResponse.class);
            return simpleResponse.getMessage();
        } catch (Exception e){
            log.error("메시지 바디 직렬화 실패");
            // 이미 실패한 상황이라 serializeBody 까지 끝나야 원인 알수 있음
            return "서버에서 오류가 발생했습니다.";
            //return e.getMessage();
        }
    }

}
