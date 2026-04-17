package org.example.tamaapi.common.exception;

public class HttpFailException extends RuntimeException {

    public HttpFailException(){
        super("http 호출을 실패했습니다");
    }
}
