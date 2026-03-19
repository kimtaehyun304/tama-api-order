package org.example.tamaapi.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.tamaapi.command.OutboxRepository;
import org.example.tamaapi.command.order.OutboxService;
import org.example.tamaapi.common.util.ThreadUtil;
import org.example.tamaapi.domain.outbox.Outbox;
import org.example.tamaapi.domain.outbox.OutboxStatus;
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
    @Scheduled(fixedDelay = 1000)
    public void publishMessages() {
        List<Long> orderIds = outboxRepository.findTop100SkipLocked(OutboxStatus.PENDING.toString())
                .stream().map(Outbox::getAggregateId).toList();

        if(!orderIds.isEmpty()) {
            orderEventProducer.produceCompletableOrderCreatedEvents(orderIds);
            outboxService.updateOutboxStatusToSent(orderIds);
        }
    }

    /*
    //멀티 인스턴스 상황을 재연하기 위해 폴링 스케줄러 추가
    //스케줄러 디폴트는 싱글 쓰레드라 멀티로 변경 필요)
    //구매 확정 스케줄러 있어서 멀티 쓰레드 유지
    @Scheduled(fixedDelay = 1000)
    public void publishMessages2() {
        List<Long> orderIds = outboxRepository.findTop100SkipLocked(OutboxStatus.PENDING.toString())
                .stream().map(Outbox::getAggregateId).toList();

        if(!orderIds.isEmpty()) {
            orderEventProducer.produceCompletableOrderCreatedEvents(orderIds);
            outboxService.updateOutboxStatusToSent(orderIds);
        }
    }
     */

}
