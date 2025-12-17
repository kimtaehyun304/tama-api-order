package org.example.tamaapi.common.auth;

import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;

import static org.example.tamaapi.common.filter.TokenAuthenticationFilter.TOKEN_PREFIX;

@Getter
@ToString
public class CustomPrincipal {
    private final String bearerJwt;
    private final Long memberId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomPrincipal(String bearerJwt, Long userId, Collection<? extends GrantedAuthority> authorities) {
        this.bearerJwt = bearerJwt;
        this.memberId = userId;
        this.authorities = authorities;
    }

}
