package org.example.tamaapi.common.config;

import org.example.tamaapi.common.dto.CustomSort;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToCustomSortConverter());
    }

    private class StringToCustomSortConverter implements Converter<String, CustomSort> {

        //검증은 SortValidator에서 함 (컨버터 역할 줄이기)
        @Override
        public CustomSort convert(String source) {

            CustomSort mySort = new CustomSort(null, null);
            String[] parts = source.split(",");

            switch (parts.length){
                case 1 -> mySort = new CustomSort(parts[0], null);
                case 2 -> mySort = new CustomSort(parts[0], Sort.Direction.fromString(parts[1]));
            }

            return mySort;
        }
    }


}
