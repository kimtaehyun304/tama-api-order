package org.tama.tamaapi.command.order;

import org.tama.tamaapi.domain.order.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {


}
