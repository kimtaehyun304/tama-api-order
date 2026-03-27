package org.example.tamaapi.command.order;

import jakarta.persistence.EntityManager;
import org.example.tamaapi.common.aspect.LogExecutionTime;
import org.example.tamaapi.domain.order.Delivery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@Transactional
class OrderServiceTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private EntityManager em;


    @Test
    @LogExecutionTime
    void 주문_동시_테스트() throws InterruptedException {
        int threadCount = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);


        String url = "http://localhost:" + 5002 + "/api/orders/free/member";

        String body = """
    {
      "paymentId": null,
      "senderNickname": "박유빈",
      "senderEmail": "'burnaby033@naver.com'",
      "receiverNickname": "박유빈",
      "receiverPhone": "'01011111113'",
      "zipCode": "12345",
      "streetAddress": "서울특별시 강남구 테헤란로",
      "detailAddress": "101동 1001호",
      "deliveryMessage": "문 앞에 놓아주세요",
      "memberCouponId": null,
      "usedPoint": 79800,
      "orderItems": [
        {"colorItemSizeStockId": 1, "orderCount": 1},
        {"colorItemSizeStockId": 2, "orderCount": 1}
      ]
    }
    """;

        // RestTemplate는 스레드 안전하므로 한 번만 생성해서 재사용
        var restTemplate = new org.springframework.web.client.RestTemplate();

        // 실패 결과를 수집할 스레드 안전한 컬렉션
        java.util.concurrent.ConcurrentLinkedQueue<String> failures = new java.util.concurrent.ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    var headers = new org.springframework.http.HttpHeaders();
                    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    //2026-03-20 ~ 2027-03-20 유효기간 1년 토큰 (체험용 계정)
                    headers.setBearerAuth("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJraW1hcGJlbEBnbWFpbC5jb20iLCJpYXQiOjE3NzM5MzM5MzksImV4cCI6MTgwNTQ2OTkzOSwic3ViIjoiMyJ9.QOAz6094LNu2_jy0WMfebbFwgCj0PUpB3hi05oqOnt4");

                    var request = new org.springframework.http.HttpEntity<>(body, headers);

                    var resp = restTemplate.postForEntity(url, request, String.class);

                    // 상태 코드로 간단 체크: 2xx가 아니면 실패 수집
                    if (!resp.getStatusCode().is2xxSuccessful()) {
                        failures.add("Status: " + resp.getStatusCodeValue());
                    }

                } catch (Exception e) {
                    // 예외는 무시하지 말고 수집 또는 로깅
                    failures.add("Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업 완료 대기
        latch.await();

        // Executor 정리
        executorService.shutdown();
        if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        // 실패가 있다면 테스트 실패로 처리
        if (!failures.isEmpty()) {
            // 실패 내용을 출력(디버깅용) 후 assert
            failures.forEach(System.out::println);
            org.junit.jupiter.api.Assertions.fail("동시 요청 중 일부 실패: count=" + failures.size());
        }

        // 추가로 응답 바디 검증 등 필요시 assertions 추가
    }


}