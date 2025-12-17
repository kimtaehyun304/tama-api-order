package org.example.tamaapi.event;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.order.OrderStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final KafkaTemplate<String, OrderFailedEvent> kafkaTemplate;
    private final String ORDER_TOPIC = "order_topic";
    /*
    public void consumeOrderFailedEvent(OrderFailedEvent event){
        OrderFailedEvent orderFailedEvent = new OrderFailedEvent(event.orderId());
        kafkaTemplate.send(ORDER_TOPIC, orderFailedEvent);
    }
    */


}
