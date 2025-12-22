package org.example.tamaapi.command.order;

import jakarta.persistence.EntityManager;
import org.example.tamaapi.domain.order.Delivery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

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

        Thread.sleep(1000*30);
        System.out.println("delivery.getId() = " + delivery.getId());
    }

    //data jap save()는 @Transactional이 붙어있다.
    //persist 시점에 insert sql 실행되고, save() 종료시 커밋하여 db에 반영
    //@Test
    void 싱글_트랜잭션() throws InterruptedException {
        Delivery delivery = new Delivery("36265", "산타마을", "오두막", "문 앞 배송 바람", "산타", "민수");
        deliveryRepository.save(delivery);

        Thread.sleep(1000*30);
        System.out.println("delivery.getId() = " + delivery.getId());
    }
}