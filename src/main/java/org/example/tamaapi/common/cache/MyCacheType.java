package org.example.tamaapi.common.cache;

import lombok.Getter;

@Getter
public enum MyCacheType {

    //JWT 토큰과 바꾸기 위한 임시 토큰
    SIGN_IN_TEMP_TOKEN(60*3, 10000),

    //회원가입시 이메일로 전송되는 인증 문자
    SIGN_UP_AUTH_STRING(60*3, 10000),

    //부하 테스트 결과, TPS 14 길래 캐시 적용
    BEST_ITEM(60*60*24, 10000);

    private final int expireAfterWrite;
    private final int maximumSize;

    MyCacheType(int expireAfterWrite, int maximumSize) {
        this.expireAfterWrite = expireAfterWrite;
        this.maximumSize = maximumSize;
    }

}
