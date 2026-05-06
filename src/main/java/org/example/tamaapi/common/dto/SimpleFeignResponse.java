package org.example.tamaapi.common.dto;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SimpleFeignResponse {
    private String code;
    private String message;
}
