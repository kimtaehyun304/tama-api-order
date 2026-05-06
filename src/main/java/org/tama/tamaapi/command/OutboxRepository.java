package org.tama.tamaapi.command;

import org.tama.tamaapi.domain.outbox.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {


    //before: List<Outbox> findTop100ByStatusOrderByIdAsc(OutboxStatus status)
    //서버 스케일 아웃 환경에서의 카프카 이벤트 중복 컨슘 방지
    @Transactional
    @Query(value = "SELECT * FROM outbox " +
            "WHERE status = :status " +
            "ORDER BY outbox_id LIMIT 100 FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<Outbox> findTop100Event(String status);

}
