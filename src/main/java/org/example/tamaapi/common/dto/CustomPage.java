package org.example.tamaapi.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class CustomPage<T> {

    private final List<T> content;

    @JsonProperty("page")
    private CustomPageable myPageable;

    //spring data jpa Page 커스텀
    public CustomPage(List<T> content, Pageable pageable, long totalPages, long totalElements) {
        this.content = content;
        myPageable = new CustomPageable(pageable, totalPages, totalElements);
    }

    //직접 만든 페이징.
    public CustomPage(List<T> content, CustomPageRequest customPageRequest, Long rowCount) {
        this.content = content;
        myPageable = new CustomPageable(customPageRequest.getPage(), customPageRequest.getSize(), rowCount);
    }



}


