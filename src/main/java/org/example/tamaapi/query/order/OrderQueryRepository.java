package org.example.tamaapi.query.order;

import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface OrderQueryRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPaymentId(String paymentId);

    @Query("select o from Order o join fetch o.orderItems oi join fetch o.delivery d where o.id = :orderId")
    Optional<Order> findAllWithOrderItemAndDeliveryByOrderId(Long orderId);

    @Query("select o from Order o join fetch o.orderItems oi join fetch o.delivery d where o.id = :orderId")
    Optional<Order> findFullByOrderId(Long orderId);

}
