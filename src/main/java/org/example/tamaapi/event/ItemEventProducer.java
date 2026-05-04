package org.example.tamaapi.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.outbox.Outbox;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemEventProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, IncreaseStockEvent> kafkaTemplate;
    private final String ITEM_TOPIC = "item_topic";
    private final String ITEM_DELAY_TOPIC = "item_delay_topic";
    long DELAY_MILLS = 1000 * 30;

    /*
    public List<Long> produceIncreaseStockEvents(List<Outbox> outboxes) {
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        //whenComplete + 일반 arraylist.add()는 동시성 이슈라 안씀
        for (Outbox outbox : outboxes) {
                List<ItemOrderCountRequest> requests = objectMapper.convertValue(outbox, new TypeReference<>() {});
                IncreaseStockEvent event = new IncreaseStockEvent(requests);
                CompletableFuture<Long> future =
                        kafkaTemplate.send(ITEM_TOPIC, event)
                                .thenApply(result -> outbox.getId()) // 성공 → outBoxId 반환
                                .exceptionally(ex -> {
                                    log.error("{} Kafka 발송 실패. outboxId={}, orderId={}",
                                            ITEM_TOPIC, outbox.getId(), outbox.getAggregateId());
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
    */

    public void produceIncreaseStockEvent(IncreaseStockEvent event){
        try {
            //동기로해야 try-catch 가능
            kafkaTemplate.send(ITEM_TOPIC, event).get();
        } catch (Exception e){
            log.error("[ITEM_TOPIC 발행 실패] 지연 이벤트 발행 {}", e.getMessage());
            kafkaTemplate.send(ITEM_DELAY_TOPIC, event);
        }
    }

    public void produceDelayIncreaseStockEvent(IncreaseStockEvent event) {
        try {
            // long -> byte[] 변환
            ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
            long now = LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
            buf.putLong(now+DELAY_MILLS);
            byte[] executeAtByte = buf.array();

            Message<IncreaseStockEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, "item_delay_topic")
                    .setHeader("x-execute-at", executeAtByte)
                    .build();
            kafkaTemplate.send(message).get();
        } catch (Exception e) {
            //로그 모니터링 구축하도 클릭해서 이벤트 발행하게 해야함 (producer dead letter 대체)
            log.error("[ITEM_DELAY_TOPIC 발행 실패] {}", e.getMessage());
        }
    }

}