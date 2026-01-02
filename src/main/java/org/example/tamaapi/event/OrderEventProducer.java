package org.example.tamaapi.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

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
            Thread.sleep(1000);
        } catch (Exception e){
            log.error("카프카 발송 실패. 이유={}",e.getMessage());
        }
    }

    public void produceOrderCreatedEvent(Long orderId){
        try {
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(orderId);
            kafkaTemplate.send(ORDER_TOPIC, orderCreatedEvent);
            //consume 시간 기다리기 위해
            Thread.sleep(1000);
        } catch (Exception e){
            log.error("카프카 발송 실패. 이유={}",e.getMessage());
        }
    }

}