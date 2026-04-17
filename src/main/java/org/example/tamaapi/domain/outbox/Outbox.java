package org.example.tamaapi.domain.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.EventType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    private Long id;

    private Long aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private JsonNode payload;

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