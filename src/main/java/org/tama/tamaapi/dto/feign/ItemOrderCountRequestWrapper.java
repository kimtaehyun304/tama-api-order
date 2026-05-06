package org.tama.tamaapi.dto.feign;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.tama.tamaapi.feignClient.item.ItemOrderCountRequest;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOrderCountRequestWrapper {

    @NotEmpty
    private List<ItemOrderCountRequest> itemOrderCountRequests;

}
