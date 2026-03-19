package org.example.tamaapi.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.common.util.ThreadUtil;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {
    private final ThreadUtil threadUtil;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String ORDER_TOPIC = "order_topic";

    @Async
    public void produceAsyncOrderCreatedEvent(Long orderId) {
        try {
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(orderId);
            kafkaTemplate.send(ORDER_TOPIC, orderCreatedEvent);
        } catch (Exception e) {
            log.error("카프카 발송 실패. 이유={}", e.getMessage());
        }
    }

    public void produceOrderCreatedEvent(Long orderId) {
        try {
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(orderId);
            kafkaTemplate.send(ORDER_TOPIC, orderCreatedEvent);
        } catch (Exception e) {
            log.error("카프카 발송 실패. 이유={}", e.getMessage());
        }
    }

    public List<Long> produceCompletableOrderCreatedEvents(List<Long> orderIds) {
        System.out.println("produceCompletableOrderCreatedEvents");
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        //whenComplete + 일반 arraylist.add()는 동시성 이슈
        for (Long orderId : orderIds) {
            OrderCreatedEvent event = new OrderCreatedEvent(orderId);

            CompletableFuture<Long> future =
                    kafkaTemplate.send(ORDER_TOPIC, orderId.toString(), event)
                            .thenApply(result -> orderId) // 성공 → orderId 반환
                            .exceptionally(ex -> {
                                log.error("Kafka 발송 실패. orderId={}, topic={}",
                                        orderId, ORDER_TOPIC, ex);
                                return null; // 실패 → null
                            });

            futures.add(future);
        }

        // 모든 Kafka 전송 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 성공한 것만 반환
        return futures.stream()
                .map(CompletableFuture::join) // 결과 꺼냄
                .filter(Objects::nonNull)     // 성공만
                .toList();
    }

}