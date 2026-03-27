package org.example.tamaapi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Service
@RequiredArgsConstructor
@Slf4j
public class MemberEventProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, RollbackCouponAndPointEvent> kafkaTemplate;
    private final String MEMBER_TOPIC = "member_topic";
    private final String MEMBER_DELAY_TOPIC = "member_delay_topic";
    long DELAY_MILLS = 1000 * 30;

    public void produceCouponAndPointRollbackEvent(RollbackCouponAndPointEvent event){
        try {
            //동기로해야 try-catch 가능
            kafkaTemplate.send(MEMBER_TOPIC, event).get();
        } catch (Exception e){
            log.error("카프카 발송 실패. 이유={}",e.getMessage());
        }
    }

    public void produceDelayRollbackCouponAndPointEvent(RollbackCouponAndPointEvent event){
        try {
            // long -> byte[] 변환
            ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
            long now = LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
            buf.putLong(now+DELAY_MILLS);
            byte[] executeAtByte = buf.array();

            Message<RollbackCouponAndPointEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, MEMBER_DELAY_TOPIC)
                    .setHeader("x-execute-at", executeAtByte)
                    .build();

            // 동기 발송(테스트/특수 케이스용). 운영에서는 주의 필요
            kafkaTemplate.send(message).get();
        } catch (Exception e){
            log.error("카프카 발송 실패. 이유={}",e.getMessage());
        }
    }

}