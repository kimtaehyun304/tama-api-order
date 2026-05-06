package org.tama.tamaapi.query.order;

import org.tama.tamaapi.domain.order.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface DeliveryQueryRepository extends JpaRepository<Delivery, Long> {


}
