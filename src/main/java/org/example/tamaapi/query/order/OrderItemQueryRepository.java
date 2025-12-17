package org.example.tamaapi.query.order;

import org.example.tamaapi.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface OrderItemQueryRepository extends JpaRepository<OrderItem, Long> {

    @Query("select oi from OrderItem oi join fetch oi.order o where oi.order.id =:orderId")
    List<OrderItem> findAllWithOrderByOrderId(Long orderId);

    @Query("select oi from OrderItem oi join fetch oi.order o where oi.id =:orderItemId")
    Optional<OrderItem> findWithOrderById(Long orderItemId);

}
