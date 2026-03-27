package org.example.tamaapi.dto.responseDto;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SimpleFeignResponse {
    private String code;
    private String message;
}
