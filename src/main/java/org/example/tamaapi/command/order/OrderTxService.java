package org.example.tamaapi.command.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.command.OutboxRepository;
import org.example.tamaapi.domain.EventType;
import org.example.tamaapi.domain.order.*;
import org.example.tamaapi.domain.outbox.Outbox;
import org.example.tamaapi.dto.requestDto.order.PortOneOrderItem;
import org.example.tamaapi.feignClient.item.ItemFeignClient;
import org.example.tamaapi.feignClient.item.ItemPriceResponse;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.tamaapi.exception.ErrorMessageUtil.NOT_FOUND_ORDER;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderTxService {


    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ItemFeignClient itemFeignClient;

    public Long saveOrder(String paymentId, Long memberId,
                           Guest guest, String receiverNickname,
                           String receiverPhone, String zipCode,
                           String streetAddress, String detailAddress,
                           String message, int shippingFee,
                           Long memberCouponId, int usedCouponPrice,
                           int usedPoint, List<PortOneOrderItem> portOneOrderItems) {
        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
        List<OrderItem> orderItems = createOrderItem(portOneOrderItems);

        Order order = (memberId != null)
                    ? Order.createMemberOrder(paymentId, memberId, delivery, memberCouponId, usedCouponPrice, usedPoint, shippingFee, orderItems)
                : Order.createGuestOrder(paymentId, guest, delivery, shippingFee, orderItems);
        
        //트랜잭션 전파로 인해 즉시 insert 쿼리 발생
        orderRepository.save(order);
        saveOrderItems(orderItems);
        //아웃박스 패턴은 공통 db 동기화를 위해 사용
        outboxRepository.save(new Outbox(order.getId(), EventType.ORDER_RECEIVED));
        return order.getId();
    }

    public void saveOrderItems(List<OrderItem> orderItems) {
        jdbcTemplate.batchUpdate("INSERT INTO order_item(order_id, color_item_size_stock_id, order_price, count) values (?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, orderItems.get(i).getOrder().getId());
                ps.setLong(2, orderItems.get(i).getColorItemSizeStockId());
                ps.setInt(3, orderItems.get(i).getOrderPrice());
                ps.setInt(4, orderItems.get(i).getCount());
            }

            @Override
            public int getBatchSize() {
                return orderItems.size();
            }
        });
    }

    //saveOrder 공통 로직
    //재고 감소는 이벤트 item msa에서
    private List<OrderItem> createOrderItem(List<PortOneOrderItem> portOneOrderItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        List<Long> colorItemSizeStockIds = portOneOrderItems.stream().map(PortOneOrderItem::getColorItemSizeStockId).toList();

        List<ItemPriceResponse> itemsPriceResponses = itemFeignClient.getItemsPrice(colorItemSizeStockIds);
        Map<Long, Integer> map = itemsPriceResponses.stream()
                .collect(Collectors.toMap(
                        ItemPriceResponse::getColorItemSizeStockId,
                        ItemPriceResponse::getPrice
                ));

        for (PortOneOrderItem portOneOrderItem : portOneOrderItems) {
            Long colorItemSizeStockId = portOneOrderItem.getColorItemSizeStockId();

            //가격 변동 or 할인 쿠폰 고려
            int orderPrice = map.get(colorItemSizeStockId);

            OrderItem orderItem = OrderItem.builder().colorItemSizeStockId(colorItemSizeStockId)
                    .orderPrice(orderPrice).count(portOneOrderItem.getOrderCount()).build();
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    public void updateOrderStatusAndSaveOutBox(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        order.updateStatus(status);
        outboxRepository.save(new Outbox(orderId, EventType.valueOf(status.name())));
    }

}