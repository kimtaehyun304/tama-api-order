package org.example.tamaapi.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Embeddable
public class Guest {

    @Column(name = "guest_nickname")
    private String nickname;

    @Column(name = "guest_email")
    private String email;

    //private String phone;

}
