package org.example.tamaapi.command.order;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.common.exception.feign.ItemFeignCommandException;
import org.example.tamaapi.common.exception.feign.MemberFeignCommandException;
import org.example.tamaapi.domain.order.*;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.feign.ItemOrderCountRequestWrapper;
import org.example.tamaapi.dto.feign.UsedCouponAndPointRequest;
import org.example.tamaapi.dto.requestDto.order.PortOneOrderItem;
import org.example.tamaapi.common.exception.UsedPaymentIdException;
import org.example.tamaapi.common.exception.OrderFailException;
import org.example.tamaapi.common.exception.WillCancelPaymentException;
import org.example.tamaapi.event.OrderEventProducer;
import org.example.tamaapi.feignClient.item.ItemFeignClient;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.example.tamaapi.feignClient.item.ItemPriceResponse;
import org.example.tamaapi.feignClient.member.Authority;
import org.example.tamaapi.feignClient.member.MemberFeignClient;
import org.example.tamaapi.query.order.OrderQueryRepository;
import org.example.tamaapi.command.PortOneService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.tamaapi.common.filter.TokenAuthenticationFilter.TOKEN_PREFIX;
import static org.example.tamaapi.common.util.ErrorMessageUtil.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderQueryRepository orderQueryRepository;
    private final OrderRepository orderRepository;

    private final MemberFeignClient memberFeignClient;
    private final ItemFeignClient itemFeignClient;

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager em;
    private final PortOneService portOneService;

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;

    public static Double REWARD_POINT_RATE = 0.005;

    public Long saveMemberOrder(String paymentId, Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                Long memberCouponId,
                                Integer usedPoint,
                                List<PortOneOrderItem> orderItems,
                                String bearerJwt) {
        List<ItemOrderCountRequest> requests = orderItems.stream().map(ItemOrderCountRequest::new).toList();
        UsedCouponAndPointRequest usedCouponAndPointRequest = null;
        try {
            int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
            int usedCouponPrice = (memberCouponId == null) ? 0 : memberFeignClient.getCouponPrice(memberCouponId, orderItemsPrice);
            int shippingFee = getShippingFee(orderItemsPrice);

            //주문 저장
            Long orderId = saveOrder(paymentId, memberId, null, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, shippingFee, memberCouponId, usedCouponPrice, usedPoint, orderItems);

            //재고 감소
            itemFeignClient.decreaseStocks(requests);

            //쿠폰 감소
            if (memberCouponId != null || usedPoint != 0) {
                int rewardPoint = (int) ((orderItemsPrice - usedCouponPrice - usedPoint) * REWARD_POINT_RATE);
                usedCouponAndPointRequest = new UsedCouponAndPointRequest(memberCouponId, usedCouponPrice, usedPoint, rewardPoint, orderItemsPrice);
                memberFeignClient.useCouponAndPoint(usedCouponAndPointRequest, bearerJwt);
            }

            return orderId;
        } catch (ItemFeignCommandException e) {
            //주문 롤백 (예외 던지면)
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (MemberFeignCommandException e) {
            //재고 롤백
            try {
                itemFeignClient.increaseStocks(requests);
            } catch (ItemFeignCommandException failedRollback) {
                //정합성은 수작업으로 맞춰야 함
                log.error("[재고 롤백 실패] requests = {}", requests);
                portOneService.cancelPayment(paymentId, e.getMessage());
                throw new WillCancelPaymentException(e.getMessage());
            }
            //쿠폰은 롤백 안 해도됨 (호출 실패했기 때문)
            //주문 롤백 (예외 던지면)
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (Exception e) {
            //이벤트 발행 실패 or 기타 (두 케이스를 분리해야 할거 같은데)
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        }
    }

    //결제 금액이 없어서 결제 취소할 일이 없어서 메서드 분리
    public Long saveMemberFreeOrder(Long memberId,
                                    String receiverNickname,
                                    String receiverPhone,
                                    String zipCode,
                                    String streetAddress,
                                    String detailAddress,
                                    String message,
                                    Long memberCouponId,
                                    Integer usedPoint,
                                    List<PortOneOrderItem> orderItems,
                                    String bearerJwt) {
        List<ItemOrderCountRequest> requests = orderItems.stream().map(ItemOrderCountRequest::new).toList();
        UsedCouponAndPointRequest usedCouponAndPointRequest = null;
        try {
            int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
            int usedCouponPrice = (memberCouponId == null) ? 0 : memberFeignClient.getCouponPrice(memberCouponId, orderItemsPrice);
            int shippingFee = getShippingFee(orderItemsPrice);

            //주문 저장
            Long orderId = saveOrder(null, memberId, null, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, shippingFee, memberCouponId, usedCouponPrice, usedPoint, orderItems);

            //재고 감소
            itemFeignClient.decreaseStocks(requests);

            //쿠폰,포인트 사용
            if (memberCouponId != null || usedPoint != 0) {
                int rewardPoint = (int) ((orderItemsPrice - usedCouponPrice - usedPoint) * REWARD_POINT_RATE);
                usedCouponAndPointRequest = new UsedCouponAndPointRequest(memberCouponId, usedCouponPrice, usedPoint, rewardPoint, orderItemsPrice);
                memberFeignClient.useCouponAndPoint(usedCouponAndPointRequest, bearerJwt);
            }

            //포인트 적립 X (무료 주문이라)
            return orderId;
        } catch (ItemFeignCommandException e) {
            //주문 롤백 (예외 던지면)
            throw new OrderFailException(e.getMessage());
        } catch (MemberFeignCommandException e) {
            //재고 롤백
            try {
                itemFeignClient.increaseStocks(requests);
            } catch (ItemFeignCommandException failedRollback) {
                //정합성은 수작업으로 맞춰야 함
                log.error("[재고 롤백 실패] requests = {}", requests);
                throw new OrderFailException(e.getMessage());
            }
            //쿠폰은 롤백 안 해도됨 (호출 실패했기 때문)
            //주문 롤백 (예외 던지면)
            throw new OrderFailException(e.getMessage());
        } catch (Exception e) {
            //이벤트 발행 실패 or 기타 (두 케이스를 분리해야 할거 같은데)
            throw new OrderFailException(e.getMessage());
        }
    }

    public Long saveGuestOrder(String paymentId,
                               String senderNickname,
                               String senderEmail,
                               String receiverNickname,
                               String receiverPhone,
                               String zipCode,
                               String streetAddress,
                               String detailAddress,
                               String message,
                               List<PortOneOrderItem> orderItems) {
        List<ItemOrderCountRequest> requests = orderItems.stream().map(ItemOrderCountRequest::new).toList();
        try {
            Guest guest = new Guest(senderNickname, senderEmail);
            int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
            int shippingFee = getShippingFee(orderItemsPrice);

            //주문 저장
            Long orderId = saveOrder(paymentId, null, guest, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, shippingFee, null, 0, 0, orderItems);

            //재고 감소
            itemFeignClient.decreaseStocks(requests);

            //비회원은 쿠폰, 포인트를 쓸 수 없어서 생략
            return orderId;
        } catch (ItemFeignCommandException e) {
            //주문 롤백 (예외 던지면)
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (Exception e) {
            //이벤트 발행 실패 or 기타 (두 케이스를 분리해야 할거 같은데)
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        }
    }

    private Long saveOrder(String paymentId,
                           Long memberId,
                           Guest guest,
                           String receiverNickname,
                           String receiverPhone,
                           String zipCode,
                           String streetAddress,
                           String detailAddress,
                           String message,
                           int shippingFee,
                           Long memberCouponId,
                           int usedCouponPrice,
                           int usedPoint,
                           List<PortOneOrderItem> portOneOrderItems) {

        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
        List<OrderItem> orderItems = createOrderItem(portOneOrderItems);


        Order order = (memberId != null)
                    ? Order.createMemberOrder(paymentId, memberId, delivery, memberCouponId, usedCouponPrice, usedPoint, shippingFee, orderItems)
                : Order.createGuestOrder(paymentId, guest, delivery, shippingFee, orderItems);

        orderRepository.save(order);
        saveOrderItems(orderItems);
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

    public void cancelGuestOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        OrderStatus status = order.getStatus();
        //나머지 케이스는 취소 불가
        if (!(status == OrderStatus.ORDER_RECEIVED || status == OrderStatus.DELIVERED)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }

    public void cancelMemberOrder(Long orderId, Long memberId, String bearerJwt, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));

        boolean isOwner = order.getMemberId().equals(memberId);
        Authority authority = memberFeignClient.findAuthority(bearerJwt);

        if (!authority.equals(Authority.ADMIN) && !isOwner) {
            String message = "주문한 사용자가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        OrderStatus status = order.getStatus();
        //나머지 케이스는 취소 불가 (운영자여도 마찬가지)
        if (!(status == OrderStatus.ORDER_RECEIVED || status == OrderStatus.DELIVERED)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
    }

    //payment == null 이면 freeOrder로 취급할수 있지만, 데이터 정합성이 안맞을 수도 있음
    public void cancelMemberFreeOrder(Long orderId, Long memberId, String bearerJwt) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));

        boolean isOwner = order.getMemberId().equals(memberId);
        Authority authority = memberFeignClient.findAuthority(bearerJwt);

        if (!authority.equals(Authority.ADMIN) && !isOwner) {
            String message = "주문한 사용자가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        OrderStatus status = order.getStatus();
        //나머지 케이스는 취소 불가 (운영자여도 마찬가지)
        if (!(status == OrderStatus.ORDER_RECEIVED || status == OrderStatus.DELIVERED)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
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

    public int getShippingFee(int orderItemsPrice) {
        return orderItemsPrice > 40000 ? 0 : 3000;
    }

    public void validateMemberOrder(PortOneOrder order, int clientTotal, Long memberId) {
        try {
            validateMemberId(memberId);
            validatePaymentId(order.getPaymentId());
            List<ItemOrderCountRequest> requests = order.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();
            //int orderItemsPrice = itemFeignClient.getTotalPrice(new ItemOrderCountRequestWrapper(requests));
            int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
            validateMemberOrderPrice(orderItemsPrice, order.getMemberCouponId(), order.getUsedPoint(), clientTotal, memberId);
        } catch (UsedPaymentIdException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        }
    }

    //클라이언트 위변조 검증
    private void validateMemberOrderPrice(int orderItemsPrice, Long memberCouponId, Integer usedPoint, Integer clientTotal, Long memberId) {
        validateMemberId(memberId);
        int SHIPPING_FEE = getShippingFee(orderItemsPrice);
        int couponPrice = (memberCouponId == null) ? 0 : memberFeignClient.getCouponPrice(memberCouponId, orderItemsPrice);
        int orderPriceUsedCoupon = orderItemsPrice - couponPrice;

        //이벤트에서 처리
        //validatePoint(usedPoint, memberId, orderPriceUsedCoupon, SHIPPING_FEE);

        int serverTotal = orderPriceUsedCoupon - usedPoint + SHIPPING_FEE;
        if (clientTotal != serverTotal)
            throw new OrderFailException("결제 금액이 위변조 됐습니다.");
    }

    //무료 주문은 PG사 결제를 안 거쳤으므로, 결제 취소 없음
    //1.쿠폰으로 무료 주문
    //2.포인트로 무료 주문
    //3.쿠폰+포인트로 무료 주문
    public void validateMemberFreeOrderPrice(int orderItemsPrice, Long memberCouponId, Integer usedPoint, Long memberId) {
        validateMemberId(memberId);
        int SHIPPING_FEE = getShippingFee(orderItemsPrice);
        int couponPrice = (memberCouponId == null) ? 0 : memberFeignClient.getCouponPrice(memberCouponId, orderItemsPrice);
        int orderPriceUsedCoupon = orderItemsPrice - couponPrice;

        if (usedPoint > orderPriceUsedCoupon + SHIPPING_FEE)
            throw new OrderFailException("주문 가격보다 많은 포인트를 사용할 수 없습니다");
        //이벤트에서 처리
        //validatePoint(usedPoint, memberId, orderPriceUsedCoupon, SHIPPING_FEE);

        int serverTotal = SHIPPING_FEE + orderPriceUsedCoupon - usedPoint;

        if (serverTotal != 0)
            throw new OrderFailException("결제해야 할 금액이 0원이 아닙니다.");
    }

    //현재 서비스 정책상 비회원은 쿠폰,포인트를 못 씀
    public void validateGuestOrder(PortOneOrder order, int clientTotal) {
        try {
            validatePaymentId(order.getPaymentId());
            List<ItemOrderCountRequest> requests = order.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();
            //int orderItemsPrice = itemFeignClient.getTotalPrice(new ItemOrderCountRequestWrapper(requests));
            int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
            int SHIPPING_FEE = getShippingFee(orderItemsPrice);
            int serverTotal = SHIPPING_FEE + orderItemsPrice;
            if (serverTotal != clientTotal)
                throw new OrderFailException("결제 금액이 위변조 됐습니다.");
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        }
    }


    private void validatePaymentId(String paymentId) {
        orderQueryRepository.findByPaymentId(paymentId)
                .ifPresent(order -> {
                    throw new UsedPaymentIdException();
                });
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null)
            throw new OrderFailException("memberId가 누락됐습니다");
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    public void updateOrderStatusToCompleted(List<Long> orderIds) {
        int count = em.createQuery("update Order o set o.status = :completed, o.updatedAt = now() where o.id in :orderIds")
                .setParameter("completed", OrderStatus.COMPLETED)
                .setParameter("orderIds", orderIds)
                .executeUpdate();
        log.info("{}건 자동 구매확정 처리 완료", count);
    }

}