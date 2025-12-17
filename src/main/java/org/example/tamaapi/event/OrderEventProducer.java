package org.example.tamaapi.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.OrderStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String ORDER_TOPIC = "order_topic";

    @Async
    public void produceAsyncOrderCreatedEvent(Long orderId){
        try {
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(orderId);
            kafkaTemplate.send(ORDER_TOPIC, orderCreatedEvent);
        } catch (Exception e){
            log.error("카프카 발송 실패. 이유={}",e.getMessage());
        }
    }

}