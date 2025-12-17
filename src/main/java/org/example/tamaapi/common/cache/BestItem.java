package org.example.tamaapi.common.cache;

import lombok.Getter;

@Getter
public enum BestItem {
    ALL_BEST_ITEM(null),
    OUTER_BEST_ITEM(1L),
    TOP_BEST_ITEM(5L),
    BOTTOM_BEST_ITEM(11L);

    private final Long categoryId;

    BestItem(Long categoryId) {
        this.categoryId = categoryId;
    }

}
