package org.tama.tamaapi.command.order;

import org.tama.tamaapi.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {


}
