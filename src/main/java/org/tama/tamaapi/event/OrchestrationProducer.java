package org.tama.tamaapi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestrationProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, IncreaseStockEvent> kafkaTemplate;
    private final String TOPIC = "item_topic";
    private final String DELAY_TOPIC = "item_delay_topic";

}