package org.example.tamaapi.command;

import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.domain.outbox.Outbox;
import org.example.tamaapi.domain.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {


    //before: List<Outbox> findTop100ByStatusOrderByIdAsc(OutboxStatus status)
    //카프카 이벤트 중복 소비 방지 (이벤트 내용은 같은데, 멀티 인스턴스에서 각자 발행하는거라 카프카가 중복 이벤트로 생각 안함)
    @Transactional
    @Query(value = "SELECT * FROM outbox " +
            "WHERE status = :status AND event_type = :eventType " +
            "ORDER BY outbox_id LIMIT 100 FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<Outbox> findTop100Event(String status, String eventType);

}
