package org.example.tamaapi.common.dto;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component
public class SortValidator {

    //MySort 컨버터 거쳐서 옴
    //컨버터에서 예외 던지니까 에러나서 SortValidator 만듬 -> BindingResult 필요없어서 커스텀 validator 만듬
    //
    public void validate(CustomSort sort) {
        String property = sort.getProperty();
        Sort.Direction direction = sort.getDirection();

        if(!StringUtils.hasText(property))
            throw new IllegalArgumentException("sort property는 공백일 수 없습니다.");

        if(direction == null)
            throw new IllegalArgumentException("sort direction은 공백일 수 없습니다. 'asc' 또는 'desc' 이어야 합니다.");
        else if(!direction.equals(Sort.Direction.ASC) && !direction.equals(Sort.Direction.DESC))
            throw new IllegalArgumentException("sort direction은 'asc' 또는 'desc' 이어야 합니다.");
    }

}
