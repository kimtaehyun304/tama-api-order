package org.example.tamaapi.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;


import org.example.tamaapi.common.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
//같은 VPC를 공유하는 MSA 서버만 호출하게 하기 위함
public class InternalOnlyAspect {

    private final HttpServletRequest request;

    @Before(
            "@annotation(org.example.tamaapi.common.aspect.InternalOnly) || " +
                    "@within(org.example.tamaapi.common.aspect.InternalOnly)"
    )
    public void checkInternalServer() {
        String clientIp = request.getRemoteAddr();

        if (!isPrivateIp(clientIp)) {
            //log.warn("[외부 접근 차단] IP = {}", clientIp);
            throw new UnauthorizedException("사설망에서만 호출 가능한 내부 전용 API입니다.");
            //NOT FOUND로 아에 없는 척하는 게 더 안전
        }

        /* 이렇게하면, IP 바뀌면 허용 리스트 수정해야 함
        // 허용할 내부 IP 목록 (내부 MSA 서버 IP)
        Set<String> allowList = Set.of(
                "127.0.0.1",
                "localhost",
                "10.0.0.1",
                "10.0.0.2"
        );
        if (!allowList.contains(clientIp)) {
            log.warn("[외부 접근 차단] InternalOnly API 접근 IP = {}", clientIp);
            throw new UnauthorizedException("외부에서 호출할 수 없는 내부 전용 API입니다.");
        }
        */

    }

    private boolean isPrivateIp(String ip) {
        if (ip.startsWith("127."))
            return true;

        if (ip.startsWith("10."))
            return true;

        if (ip.startsWith("192.168."))
            return true;

        // 172.16.0.0 ~ 172.31.255.255
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                return second >= 16 && second <= 31;
            } catch (Exception ignored) {}
        }
        return false;
    }
}