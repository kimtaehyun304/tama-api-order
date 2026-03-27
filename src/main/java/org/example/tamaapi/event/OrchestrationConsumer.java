package org.example.tamaapi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestrationConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, IncreaseStockEvent> kafkaTemplate;
    private final String TOPIC = "item_topic";
    private final String ITEM_DELAY_TOPIC = "item_delay_topic";

    private final ItemEventProducer itemEventProducer;

    /*
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    @KafkaListener(topics = ITEM_DELAY_TOPIC, groupId = "item_delay_consumer_group")
    //retry + ack 인데 왜 되지? order msa에선 안됐는데
    public void consumeIncreaseStockEvent(IncreaseStockEvent event, Acknowledgment ack) {
        itemEventProducer.produceIncreaseStockEvent(event);
        ack.acknowledge();
    }
    */
}