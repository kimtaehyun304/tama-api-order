package org.example.tamaapi.query.order;

import org.example.tamaapi.domain.order.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface DeliveryQueryRepository extends JpaRepository<Delivery, Long> {


}
