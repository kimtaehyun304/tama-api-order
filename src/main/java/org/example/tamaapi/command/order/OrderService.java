package org.example.tamaapi.command.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.command.OutboxRepository;
import org.example.tamaapi.common.exception.feign.RefusedDiscountException;
import org.example.tamaapi.common.exception.feign.NotEnoughStockException;
import org.example.tamaapi.domain.order.*;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.feign.UsedCouponAndPointRequest;
import org.example.tamaapi.dto.requestDto.order.PortOneOrderItem;
import org.example.tamaapi.common.exception.UsedPaymentIdException;
import org.example.tamaapi.event.*;
import org.example.tamaapi.feignClient.item.ItemFeignClient;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.example.tamaapi.feignClient.member.Authority;
import org.example.tamaapi.feignClient.member.MemberFeignClient;
import org.example.tamaapi.query.order.OrderQueryRepository;
import org.example.tamaapi.command.PortOneService;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.example.tamaapi.common.util.ErrorMessageUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderQueryRepository orderQueryRepository;
    private final OrderRepository orderRepository;
    private final OrderTxService orderTxService;
    private final EntityManager em;
    private final PortOneService portOneService;

    private final MemberFeignClient memberFeignClient;
    private final ItemFeignClient itemFeignClient;
    private final OrderEventProducer orderEventProducer;
    private final ItemEventProducer itemEventProducer;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MemberEventProducer memberEventProducer;
    public static Double REWARD_POINT_RATE = 0.005;

    /* 별도 saga 오케스트레이션 서버에서 호출하는 버전일때 만든 코드
    별도 saga 오케이스트레이션 서버를 쓰면 라우팅이 복잡해짐.
    근데 주문 msa가  재고, 회원 서버를 호출 안 하니 msa 간 강경합은 사라짐
    근데 동기 호출 아키텍처라 별도 오케스트레이션 서버라해도 어짜피 강결합이긴함
    spof 문제도 있지만 오토스케일하면되서 ㄱㅊ
    public void saveMemberOrder(int orderItemsPrice,
                                String paymentId,
                                Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                Long memberCouponId,
                                int usedCouponPrice,
                                Integer usedPoint,
                                List<PortOneOrderItem> orderItems) {
        try {
            //재고 감소
            List<ItemOrderCountRequest> requests = orderItems.stream().map(ItemOrderCountRequest::new).toList();

            int rewardPoint = (int) ((orderItemsPrice - usedCouponPrice - usedPoint) * REWARD_POINT_RATE);
            //주문 저장
            saveOrderTx(orderItemsPrice, paymentId, memberId, null, receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress,
                    message, memberCouponId, usedPoint, orderItems, usedCouponPrice, requests, rewardPoint);

            //폴링 부하를 방지하려면  message.published = true; outbox.save(message); (별 차이 없을 듯)

            //아웃박스 폴링, 이벤트 발행,소비 진행 시간 고려하여 1.5초 쯤 대기해야함. 브라우저에서 대기해야 성능 문제 없음
        } catch (Exception e) {
            portOneService.cancelPayment(paymentId, e.getMessage());
            String failMessage = "주문을 실패했습니다";
            throw new RuntimeException(failMessage);

            //아웃박스 패턴하려했는데, 아웃박스 저장 실패했을때 원자적 같이 취소할게 없어서 안했음
            //대신 이벤트 발행 실패하면 producer dead letter 브로커에 보냄

            //주문은 자동 롤백
        }
    }
    */

    public void saveMemberOrder(int orderItemsPrice,
                                String paymentId,
                                Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                Long memberCouponId,
                                Integer usedPoint,
                                List<PortOneOrderItem> orderItems) {
        try {
            //재고 감소
            List<ItemOrderCountRequest> requests = orderItems.stream().map(ItemOrderCountRequest::new).toList();
            decreaseStock(paymentId, requests);

            //쿠폰 포인트 사용
            int usedCouponPrice = (memberCouponId == null) ? 0 : memberFeignClient.getCouponPrice(memberCouponId, orderItemsPrice);
            int rewardPoint = (int) ((orderItemsPrice - usedCouponPrice - usedPoint) * REWARD_POINT_RATE);
            useCouponAndPoint(orderItemsPrice, paymentId, memberCouponId, usedPoint, memberId, usedCouponPrice, rewardPoint, requests);

            //주문 저장
            saveOrderTx(orderItemsPrice, paymentId, memberId, null, receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress,
                    message, memberCouponId, usedPoint, orderItems, usedCouponPrice, requests, rewardPoint);

            //폴링 부하를 방지하려면  message.published = true; outbox.save(message); (별 차이 없을 듯)

            //아웃박스 폴링, 이벤트 발행,소비 진행 시간 고려하여 1.5초 쯤 대기해야함. 브라우저에서 대기해야 성능 문제 없음
        } catch (Exception e) {
            portOneService.cancelPayment(paymentId, e.getMessage());
            String failMessage = "주문을 실패했습니다";
            throw new RuntimeException(failMessage);

            //아웃박스 패턴하려했는데, 아웃박스 저장 실패했을때 원자적 같이 취소할게 없어서 안했음
            //대신 이벤트 발행 실패하면 producer dead letter 브로커에 보냄

            //주문은 자동 롤백
        }
    }

    //결제 금액이 없어서 결제 취소할 일이 없어서 메서드 분리
    public void saveMemberFreeOrder(int orderItemsPrice,
                                    Long memberId,
                                    String receiverNickname,
                                    String receiverPhone,
                                    String zipCode,
                                    String streetAddress,
                                    String detailAddress,
                                    String message,
                                    Long memberCouponId,
                                    Integer usedPoint,
                                    List<PortOneOrderItem> orderItems) {
        try {
            List<ItemOrderCountRequest> requests = orderItems.stream().map(ItemOrderCountRequest::new).toList();
            //재고 감소
            String paymentId = "free-order-" + UUID.randomUUID();
            decreaseStock(paymentId, requests);

            //쿠폰 포인트 사용
            int usedCouponPrice = (memberCouponId == null) ? 0 : memberFeignClient.getCouponPrice(memberCouponId, orderItemsPrice);
            int rewardPoint = (int) ((orderItemsPrice - usedCouponPrice - usedPoint) * REWARD_POINT_RATE);
            useCouponAndPoint(orderItemsPrice, paymentId, memberCouponId, usedPoint, memberId, usedCouponPrice, rewardPoint, requests);

            saveOrderTx(orderItemsPrice, paymentId, memberId, null, receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress
                    , message, memberCouponId, usedPoint, orderItems, usedCouponPrice, requests, rewardPoint);

            //포인트 적립 X (무료 주문이라)
        } catch (Exception e) {
            String failMessage = String.format("주문을 실패했습니다. %s", e.getMessage());
            throw new RuntimeException(failMessage);
        }
    }

    public Long saveGuestOrder(int orderItemsPrice,
                               String paymentId,
                               String senderNickname,
                               String senderEmail,
                               String receiverNickname,
                               String receiverPhone,
                               String zipCode,
                               String streetAddress,
                               String detailAddress,
                               String message,
                               List<PortOneOrderItem> orderItems) {
        try {
            //재고 감소
            List<ItemOrderCountRequest> requests = orderItems.stream().map(ItemOrderCountRequest::new).toList();
            decreaseStock(paymentId, requests);

            Guest guest = new Guest(senderNickname, senderEmail);
            Long orderId = saveGuestOrderTx(orderItemsPrice, paymentId, guest, receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress, message, orderItems, requests);

            return orderId;
        } catch (Exception e) {
            portOneService.cancelPayment(paymentId, e.getMessage());
            String failMessage = "주문을 실패했습니다";
            throw new RuntimeException(failMessage);
        }
    }

    private void saveOrderTx(int orderItemsPrice, String paymentId, Long memberId, Guest guest, String receiverNickname, String receiverPhone, String zipCode, String streetAddress, String detailAddress, String message, Long memberCouponId, Integer usedPoint, List<PortOneOrderItem> orderItems, int usedCouponPrice, List<ItemOrderCountRequest> requests, int rewardPoint) {
        try {
            //주문 저장
            orderTxService.saveOrder(paymentId, memberId, guest, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, getShippingFee(orderItemsPrice), memberCouponId, usedCouponPrice, usedPoint, orderItems);
        } catch (Exception e) {
            //재고 롤백
            increaseStock(paymentId, requests);
            //쿠폰, 포인트 롤백
            rollbackCouponAndPoint(paymentId, memberId, memberCouponId, usedPoint, rewardPoint, requests);
        }
    }

    //비회원은 쿠폰, 포인트를 쓸 수 없어서 생략
    private Long saveGuestOrderTx(int orderItemsPrice, String paymentId, Guest guest, String receiverNickname, String receiverPhone, String zipCode, String streetAddress, String detailAddress, String message, List<PortOneOrderItem> orderItems, List<ItemOrderCountRequest> requests) {
        try {
            return orderTxService.saveOrder(paymentId, null, guest, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, getShippingFee(orderItemsPrice), null, 0, 0, orderItems);
        } catch (Exception e) {
            //재고 롤백
            increaseStock(paymentId, requests);
            throw e;
        }
    }

    private void useCouponAndPoint(int orderItemsPrice, String paymentId, Long memberCouponId, Integer usedPoint, Long memberId, int usedCouponPrice, int rewardPoint, List<ItemOrderCountRequest> requests) {
        try {
            if (memberCouponId != null || usedPoint != 0) {
                UsedCouponAndPointRequest usedCouponAndPointRequest = new UsedCouponAndPointRequest(paymentId, memberCouponId, usedCouponPrice, usedPoint, rewardPoint, orderItemsPrice, memberId);
                memberFeignClient.useCouponAndPoint(usedCouponAndPointRequest);
            }
        } catch (RefusedDiscountException e) {
            //정상적인 실패로 쿠폰 안 사용한 상태
            throw e;
        } catch (Exception e) {
            rollbackCouponAndPoint(paymentId, memberId, memberCouponId, usedPoint, rewardPoint, requests);
        }
    }

    private void rollbackCouponAndPoint(String paymentId, Long memberId, Long memberCouponId, Integer usedPoint, int rewardPoint, List<ItemOrderCountRequest> requests) {
        RollbackCouponAndPointEvent event = new RollbackCouponAndPointEvent(paymentId, memberId, memberCouponId, usedPoint, rewardPoint);
        //server down or 1회성 네트워크 이슈 or 타임아웃 등 모든 케이스는 재고 차감은 되고 응답만 실패했을 가능성 있음
        String failMessage = "회원 서버에 에러가 발생했습니다";
        try {
            //할인 로그있으면 다음 로직 진행
            if (!memberFeignClient.existDiscountLog(paymentId)) {
                increaseStock(paymentId, requests);
                memberEventProducer.produceDelayRollbackCouponAndPointEvent(event);
                throw new RuntimeException(failMessage);
            }
        } catch (Exception e) {
            //재시도 실패시 지연 이벤트 발행 & 주문 취소를 위해 예외 던지기
            memberEventProducer.produceDelayRollbackCouponAndPointEvent(event);
            throw new RuntimeException(failMessage, e);
        }
    }

    private void decreaseStock(String paymentId, List<ItemOrderCountRequest> requests) {
        try {
            itemFeignClient.decreaseStocks(requests, paymentId);
        } catch (NotEnoughStockException e) {
            throw e;
        } catch (Exception e) {
            checkAndIncreaseStock(paymentId, requests);
        }
    }

    private void checkAndIncreaseStock(String paymentId, List<ItemOrderCountRequest> requests) {
        IncreaseStockEvent event = new IncreaseStockEvent(paymentId, requests);
        //server down or 1회성 네트워크 이슈 or 타임아웃 등 모든 케이스는 재고 차감은 되고 응답만 실패했을 가능성 있음
        String failMessage = "상품 서버에 에러가 발생했습니다";
        try {
            //타임아웃 케이스
            //1. 처리 완료되고 타잉아웃 (조회 가능)
            //2. 타임아웃 이후 처리완료 (조회 시점엔 없지만 나중에 저장되는)
            // ㄴ롤백 이벤트 소비 시점에서도 아직 저장안됐을 가능성 (지연 이벤트로 발행해야함)
            //3. 진짜 처리 못하고 서버 down (조회 불가)
            //재고 로그 있으면 계속 로직 진행
            if (!itemFeignClient.existDecreaseStockLog(paymentId)) {
                itemEventProducer.produceDelayIncreaseStockEvent(event);
                throw new RuntimeException(failMessage);
            }
        } catch (Exception e) {
            //재시도 실패시 지연 이벤트 발행 & 주문 취소를 위해 예외 던지기
            itemEventProducer.produceDelayIncreaseStockEvent(event);
            //런타임 예외 두번 감싸지는거 예방
            throw new RuntimeException(failMessage, e);
        }
    }

    //무조건 롤백시키는 목적의 메소드
    //다음 로직인 rollbackCouponAndPoint가 실행되야해서 예외 던지면 안 됨
    //어짜피 pdl 패턴이라 예외 안 던지긴하지만
    private void increaseStock(String paymentId, List<ItemOrderCountRequest> requests) {
        IncreaseStockEvent event = new IncreaseStockEvent(paymentId, requests);
        //server down or 1회성 네트워크 이슈 or 타임아웃 등 모든 케이스는 재고 차감은 되고 응답만 실패했을 가능성 있음
        try {
            if (itemFeignClient.existDecreaseStockLog(paymentId))
                itemEventProducer.produceIncreaseStockEvent(event);
            else
                itemEventProducer.produceDelayIncreaseStockEvent(event);
        } catch (Exception e) {
            //타임아웃 때문에 뒤늦게 저장되는 경우 대비
            itemEventProducer.produceDelayIncreaseStockEvent(event);
        }
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


    public int getShippingFee(int orderItemsPrice) {
        return orderItemsPrice > 40000 ? 0 : 3000;
    }

    public void validateMemberOrder(int orderItemsPrice, PortOneOrder order, int clientTotal, Long memberId) {
        try {
            validateMemberId(memberId);
            validatePaymentId(order.getPaymentId());
            List<ItemOrderCountRequest> requests = order.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();
            validateMemberOrderPrice(orderItemsPrice, order.getMemberCouponId(), order.getUsedPoint(), clientTotal, memberId);
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw e;
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
            throw new IllegalArgumentException("결제 금액이 위변조 됐습니다.");
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
            throw new IllegalArgumentException("주문 가격보다 많은 포인트를 사용할 수 없습니다");
        //이벤트에서 처리
        //validatePoint(usedPoint, memberId, orderPriceUsedCoupon, SHIPPING_FEE);

        int serverTotal = SHIPPING_FEE + orderPriceUsedCoupon - usedPoint;

        if (serverTotal != 0)
            throw new IllegalArgumentException("결제해야 할 금액이 0원이 아닙니다.");

    }

    //현재 서비스 정책상 비회원은 쿠폰,포인트를 못 씀
    public void validateGuestOrder(int orderItemsPrice, PortOneOrder order, int clientTotal) {
        try {
            validatePaymentId(order.getPaymentId());
            List<ItemOrderCountRequest> requests = order.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();
            //int orderItemsPrice = itemFeignClient.getTotalPrice(new ItemOrderCountRequestWrapper(requests));
            int SHIPPING_FEE = getShippingFee(orderItemsPrice);
            int serverTotal = SHIPPING_FEE + orderItemsPrice;
            if (serverTotal != clientTotal)
                throw new IllegalArgumentException("결제할 금액이 위변조 됐습니다.");
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            throw e;
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
            throw new IllegalArgumentException("memberId가 누락됐습니다");
    }

    //------------------------------------
    public void updateOrderStatusToCompleted(List<Long> orderIds) {
        int count = em.createQuery("update Order o set o.status = :completed, o.updatedAt = now() where o.id in :orderIds")
                .setParameter("completed", OrderStatus.COMPLETED)
                .setParameter("orderIds", orderIds)
                .executeUpdate();
        log.info("{}건 자동 구매확정 처리 완료", count);
    }

}