package org.tama.tamaapi.config.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.tama.tamaapi.feignClient.member.Authority;
import org.tama.tamaapi.feignClient.member.MemberFeignClient;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.tama.tamaapi.exception.ErrorMessageUtil;

import java.util.Collections;
import java.util.Set;

@Component
@Aspect
@RequiredArgsConstructor
//@Secured("ROLE_ADMIN")보다 먼저 실행되도록 order 지정 (@Secured의 order는 100)
//order 지정하려고 config 분리
@Order(100-1)
@Slf4j
public class PreAuthenticationAspect {

    private final MemberFeignClient memberFeignClient;

    @Before("@annotation(org.tama.tamaapi.config.aspect.PreAuthentication)")
    public void setAuthentication() {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = (Long) authentication.getPrincipal();

        //헤더에 토큰 첨부안하면 null
        if (memberId == null)
            throw new RuntimeException(ErrorMessageUtil.NOT_AUTHENTICATED);

        Authority authority = memberFeignClient.findAuthority(memberId);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(authority.getAuthority()));
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                null,
                null,  // password는 필요하지 않아서 null 지정
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

}