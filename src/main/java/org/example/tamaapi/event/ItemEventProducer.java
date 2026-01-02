package org.example.tamaapi.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemEventProducer {
    private final KafkaTemplate<String, IncreaseStockEvent> kafkaTemplate;
    private final String ITEM_TOPIC = "item_topic";


    public void produceIncreaseStockEvent(List<ItemOrderCountRequest> requests){
        try {
            IncreaseStockEvent increaseStockEvent = new IncreaseStockEvent(requests);
            kafkaTemplate.send(ITEM_TOPIC, increaseStockEvent);
        } catch (Exception e){
            log.error("카프카 발송 실패. 이유={}",e.getMessage());
        }
    }

}