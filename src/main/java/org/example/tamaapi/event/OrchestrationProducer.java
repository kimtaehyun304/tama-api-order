package org.example.tamaapi.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.tamaapi.domain.outbox.Outbox;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestrationProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, IncreaseStockEvent> kafkaTemplate;
    private final String TOPIC = "item_topic";
    private final String DELAY_TOPIC = "item_delay_topic";

}