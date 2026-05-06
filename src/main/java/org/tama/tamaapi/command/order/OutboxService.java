package org.tama.tamaapi.command.order;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tama.tamaapi.domain.outbox.OutboxStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OutboxService {

    private final EntityManager em;

    public void updateOutboxStatusToSentInId(List<Long> outBoxIds) {
        int count = em.createQuery("update Outbox o set o.status = :SENT, o.updatedAt = now() where o.id in :outBoxIds")
                .setParameter("SENT", OutboxStatus.SENT)
                .setParameter("outBoxIds", outBoxIds)
                .executeUpdate();
        /*
        if(count !=0)
            log.info("{}건 아웃박스 SENT 처리 완료", count);
         */
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