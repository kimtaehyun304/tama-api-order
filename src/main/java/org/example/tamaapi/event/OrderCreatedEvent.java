package org.example.tamaapi.event;

import org.example.tamaapi.domain.order.OrderStatus;

public record OrderCreatedEvent(Long orderId) {
}
