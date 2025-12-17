package org.example.tamaapi.common.exception.feign;

import feign.codec.ErrorDecoder;

public class ItemFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, feign.Response response) {
        String httpMethod = response.request().httpMethod().name();

        // PUT, POST, DELETE일 때만 우리 custom exception
        if (httpMethod.equals("PUT") || httpMethod.equals("POST") || httpMethod.equals("DELETE"))
            return new ItemFeignCommandException(response.body().toString());

        // 나머지(GET 등)은 기본 예외 처리
        return defaultDecoder.decode(methodKey, response);
    }
}
