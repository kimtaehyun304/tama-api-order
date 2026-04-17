package org.example.tamaapi.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.common.util.ThreadUtil;
import org.example.tamaapi.domain.EventType;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.domain.outbox.Outbox;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    //카프카는 토픽에 온 메시지를 읽는거라, 다른 이벤트여도 컨슈머가 읽을 수있어서, 다른 토픽 사용
    private final String ORDER_SYNC_TOPIC = "order_sync_topic";

    public List<Long> produceOrderEvents(List<Outbox> outboxes) {
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        //whenComplete + 일반 arraylist.add()는 동시성 이슈
        for (Outbox outbox : outboxes) {
            OrderEvent event = new OrderEvent(outbox.getEventType(), outbox.getAggregateId());
            //비동기지만 한번에 모아서 전송하는 단일 쓰레드 방식
            CompletableFuture<Long> future =
                    kafkaTemplate.send(ORDER_SYNC_TOPIC, event)
                            .thenApply(result -> outbox.getId()) // 성공 → outBoxId 반환
                            .exceptionally(ex -> {
                                log.error("Kafka 발송 실패. outboxId={}, orderId={}, topic={}",
                                        outbox.getId(), outbox.getAggregateId(), ORDER_SYNC_TOPIC);
                                return null;
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