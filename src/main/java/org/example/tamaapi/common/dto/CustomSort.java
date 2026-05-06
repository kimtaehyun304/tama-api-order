package org.example.tamaapi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
@ToString
public class CustomSort {

    private String property;

    private Sort.Direction direction;
}
