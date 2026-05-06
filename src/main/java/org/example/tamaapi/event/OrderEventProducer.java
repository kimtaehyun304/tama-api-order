package org.example.tamaapi.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.outbox.Outbox;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    //카프카는 토픽에 온 메시지를 읽는거라, 다른 이벤트여도 컨슈머가 읽을 수있어서, 다른 토픽 사용
    private final String ORDER_SYNC_TOPIC = "order_sync_topic";

    public List<Long> produceOrderEvents(List<Outbox> outboxes) {
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        //whenComplete + 일반 arraylist.add()는 동시성 이슈
        for (Outbox outbox : outboxes) {
            OrderEvent event = new OrderEvent(outbox.getEventType(), outbox.getAggregateId());

            //send가 비동기 방식이라 CompletableFuture 사용
            //1. CompletableFuture.allOf 실행시 카프카 send 시작
            // send는 비동기 방식으로 요청을 내부 큐에 쌓는건지 즉시 발행하는 게 아님
            //2. 프로듀서는 내부 큐에 쌓인 걸 모았다가 한번에 쏜다 (단일 쓰레드)
            // [kafka-producer-network-thread | producer-1] * n 이렇게 로그 뜸
            // 실제 전송은 단일 쓰레드지만, 한번에 모아서 보내므로, send 방식인 비동기로 빠르게 큐에 쌓는게 빠르다
            CompletableFuture<Long> future =
                    kafkaTemplate.send(ORDER_SYNC_TOPIC, event)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    log.error("Kafka 발송 실패. outboxId={}, orderId={}, topic={}",
                                            outbox.getId(), outbox.getAggregateId(), ORDER_SYNC_TOPIC, ex);
                                } else {
                                    /* 실험하려고 else 썼음
                                    log.info("Kafka 발송 성공. outboxId={}, partition={}, offset={}",
                                            outbox.getId(),
                                            result.getRecordMetadata().partition(),
                                            result.getRecordMetadata().offset());

                                     */
                                }
                            })
                            .thenApply(result -> outbox.getId());

            futures.add(future);
        }


        // 모든 Kafka 전송 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("발송 끝!");

        // 성공한 것만 반환
        return futures.stream()
                .map(CompletableFuture::join) // 결과 꺼냄
                .filter(Objects::nonNull)     // 성공만
                .toList();
    }


}