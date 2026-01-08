package org.example.tamaapi.command.order;

import jakarta.persistence.EntityManager;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@Transactional
class OrderServiceTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private EntityManager em;

    //커밋 전이라도, persist하면 insert sql 실행된다
    //@Test
    @Transactional
    @Rollback(false)
    void 멀티_트랜잭션() throws InterruptedException {
        Delivery delivery = new Delivery("36265", "산타마을", "오두막", "문 앞 배송 바람", "산타", "민수");
        deliveryRepository.save(delivery);

        Thread.sleep(1000 * 30);
        System.out.println("delivery.getId() = " + delivery.getId());
    }

    //data jap save()는 @Transactional이 붙어있다.
    //persist 시점에 insert sql 실행되고, save() 종료시 커밋하여 db에 반영
    //@Test
    void 싱글_트랜잭션() throws InterruptedException {
        Delivery delivery = new Delivery("36265", "산타마을", "오두막", "문 앞 배송 바람", "산타", "민수");
        deliveryRepository.save(delivery);

        Thread.sleep(1000 * 30);
        System.out.println("delivery.getId() = " + delivery.getId());
    }


    //이게 더 간단해서 쓸듯, 어짜피 네이티브 쿼리 써야해서
    //@Test
    void LocalDateTime_변환_테스트_네이티브() {
        List<Long> resultList = em.createNativeQuery("SELECT o.order_id FROM orders o WHERE o.updated_at >= now() - interval 80 day", Long.class)
                .getResultList();
        System.out.println("resultList.size() = " + resultList.size());
    }
    /*
    //@Test
    void LocalDateTime_변환_테스트_네이티브2() {
        String eightDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(80).toLocalDate().atStartOfDay()).toString();
        System.out.println("eightDaysAgo = " + eightDaysAgo);
        List<Long> list = em.createNativeQuery("SELECT o.order_id FROM orders o WHERE o.updated_at >= :eightDaysAgo", Long.class)
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
        System.out.println("list.size() = " + list.size());
    }
    */
    //@Test
    @Transactional
    @Rollback(false)
    void LocalDateTime_변환_테스트_실패() {
        System.out.println(TimeZone.getDefault());
        //IDE에서 예외는 안발생하지만, 워크벤치에서 해보면 에외 발생
        LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(80).toLocalDate().atStartOfDay();
        //이건 예외 발생
        //String eightDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(8).toLocalDate().atStartOfDay()).toString();
        System.out.println("eightDaysAgo = " + eightDaysAgo);
        List<Long> resultList = em.createQuery("SELECT o.id FROM Order o WHERE o.updatedAt >= :eightDaysAgo", Long.class)
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
        for (Long orderId : resultList) {
            System.out.println("orderId = " + orderId);
        }
        System.out.println("resultList.size() = " + resultList.size());
    }

}