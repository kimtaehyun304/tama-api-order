package org.example.tamaapi.domain.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.EventType;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    private Long id;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    //발행 실패했을때를 구분하려고
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    public Outbox(Long aggregateId, EventType eventType) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.status = OutboxStatus.PENDING;
    }

}