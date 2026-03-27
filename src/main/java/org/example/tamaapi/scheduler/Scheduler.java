package org.example.tamaapi.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.tamaapi.command.OutboxRepository;
import org.example.tamaapi.command.order.OutboxService;
import org.example.tamaapi.common.util.ThreadUtil;
import org.example.tamaapi.domain.EventType;
import org.example.tamaapi.domain.outbox.Outbox;
import org.example.tamaapi.domain.outbox.OutboxStatus;
import org.example.tamaapi.event.ItemEventProducer;
import org.example.tamaapi.event.OrderEventProducer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final JobLauncher jobLauncher;
    private final Job completeOrderJob;

    private final OutboxRepository outboxRepository;
    private final OrderEventProducer orderEventProducer;
    private final ItemEventProducer itemEventProducer;
    private final OutboxService outboxService;
    private final ThreadUtil threadUtil;

    //사용하지 않는 이미지를 주기적으로 제거하려했는데, 이미지 수정할 때 비동기로 지워주면 됨!

    //멀티 인스턴스 환경에선 배치 인스턴스 분리해야함 (AWS Lightsail 추천)
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    //@EventListener(ApplicationReadyEvent.class)
    public void runCompleteOrderJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDate("time", LocalDate.now())
                    .toJobParameters();
            jobLauncher.run(completeOrderJob, jobParameters);
        } catch (Exception e) {
            log.error("자동 구매확정 배치 실행 실패: " + e.getMessage());
        }
    }


    //멀티 쓰레드 스케줄러라서 경합을 방지하기 위해 fixedDelay 사용
    //싱글 쓰레드 스케줄러일 경우 fixedRate여도 무관
    @Scheduled(fixedDelay = 1000, zone = "Asia/Seoul")
    public void publishSyncOrderCreatedEvent() {
        List<Outbox> outboxes = outboxRepository.findTop100Event(OutboxStatus.PENDING.toString(), EventType.ORDER_CREATED.toString()).stream().toList();

        if(!outboxes.isEmpty()) {
            List<Long> sentOutboxIds = orderEventProducer.produceSyncOrderCreatedEvents(outboxes);
            outboxService.updateOutboxStatusToSentInId(sentOutboxIds);
        }
    }


}
