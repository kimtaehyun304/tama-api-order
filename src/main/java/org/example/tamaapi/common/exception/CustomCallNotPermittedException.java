package org.example.tamaapi.common.exception;

public class CustomCallNotPermittedException extends RuntimeException {

    public CustomCallNotPermittedException() {
        super("트래픽 과부하 상태입니다. 잠시 후에 이용해주세요");
    }

}
