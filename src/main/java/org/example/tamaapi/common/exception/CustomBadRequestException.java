package org.example.tamaapi.common.exception;

public class CustomBadRequestException extends RuntimeException {

    public CustomBadRequestException(){
        super("잘못된 요청입니다");
    }

    public CustomBadRequestException(String message){ super(message);}
}
