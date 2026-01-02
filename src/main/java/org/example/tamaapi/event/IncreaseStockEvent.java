package org.example.tamaapi.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class IncreaseStockEvent {

    private List<ItemOrderCountRequest> requests;

}
