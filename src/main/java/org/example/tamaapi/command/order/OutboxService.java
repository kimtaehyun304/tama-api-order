package org.example.tamaapi.command.order;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.command.PortOneService;
import org.example.tamaapi.common.exception.UsedPaymentIdException;
import org.example.tamaapi.common.exception.feign.ItemFeignCommandException;
import org.example.tamaapi.common.exception.feign.MemberFeignCommandException;
import org.example.tamaapi.common.util.ThreadUtil;
import org.example.tamaapi.domain.order.Guest;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.domain.outbox.OutboxStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.feign.UsedCouponAndPointRequest;
import org.example.tamaapi.dto.requestDto.order.PortOneOrderItem;
import org.example.tamaapi.event.ItemEventProducer;
import org.example.tamaapi.event.OrderEventProducer;
import org.example.tamaapi.feignClient.item.ItemFeignClient;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.example.tamaapi.feignClient.member.Authority;
import org.example.tamaapi.feignClient.member.MemberFeignClient;
import org.example.tamaapi.query.order.OrderQueryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.tamaapi.common.util.ErrorMessageUtil.NOT_FOUND_ORDER;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OutboxService {

    private final EntityManager em;

    //db가 asia
    public void updateOutboxStatusToSentInId(List<Long> outBoxIds) {
        int count = em.createQuery("update Outbox o set o.status = :SENT, o.updatedAt = now() where o.id in :outBoxIds")
                .setParameter("SENT", OutboxStatus.SENT)
                .setParameter("outBoxIds", outBoxIds)
                .executeUpdate();
        if(count !=0)
            log.info("{}건 아웃박스 SENT 처리 완료", count);
    }
    /*
    public void updateOutboxStatusToSent(List<Long> orderIds) {
        int count = em.createQuery("update Outbox o set o.status = :SENT, o.updatedAt = now() where o.aggregateId in :orderIds")
                .setParameter("SENT", OutboxStatus.SENT)
                .setParameter("orderIds", orderIds)
                .executeUpdate();
        if(count !=0)
            log.info("{}건 아웃박스 SENT 처리 완료", count);
    }
    */
}