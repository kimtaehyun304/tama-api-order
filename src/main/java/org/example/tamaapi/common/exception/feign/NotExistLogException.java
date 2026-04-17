package org.example.tamaapi.common.exception.feign;

public class NotExistLogException extends RuntimeException {

    public NotExistLogException(){
        super("로그가 없습니다");
    }

    public NotExistLogException(String message){
        super(message);
    }
}
