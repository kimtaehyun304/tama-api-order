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
import org.example.tamaapi.feignClient.item.ItemFeignClient;
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
    private final OutboxService outboxService;
    private final ItemFeignClient itemFeignClient;

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

    //fixedDelay 1초 지나도 작업이 종료되야 다음거 실행
    //fixedRate는 딱 시간마다 실행하므로 db 지연되면 경합 가능성
    //p.s)fixedRate 쓰러면 스케줄러 설정을 멀티 쓰레드로 바꿔야 함
    @Scheduled(fixedDelay = 1000, zone = "Asia/Seoul")
    public void publishSyncOrderEvent() {
        //서버 스케일 아웃하면, 스케줄러 동시에 여러개 실행되므로 스킵락 사용
        List<Outbox> outboxes = outboxRepository.findTop100Event(OutboxStatus.PENDING.toString()).stream().toList();

        if(!outboxes.isEmpty()) {
            List<Long> sentOutboxIds = orderEventProducer.produceOrderEvents(outboxes);
            outboxService.updateOutboxStatusToSentInId(sentOutboxIds);
        }
    }

}
