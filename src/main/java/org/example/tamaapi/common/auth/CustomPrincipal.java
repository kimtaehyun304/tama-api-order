package org.example.tamaapi.common.auth;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;

import static org.example.tamaapi.common.filter.TokenAuthenticationFilter.TOKEN_PREFIX;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CustomPrincipal {
    private String bearerJwt;
    private Long memberId;
}
