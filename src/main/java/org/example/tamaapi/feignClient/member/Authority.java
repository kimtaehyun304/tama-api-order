package org.example.tamaapi.feignClient.member;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    Authority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
