package org.example.tamaapi.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.EventType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private EventType eventType;
    private Long orderId;

}
