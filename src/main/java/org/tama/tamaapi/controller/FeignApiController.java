package org.tama.tamaapi.controller;

import lombok.RequiredArgsConstructor;
import org.tama.sharelib.common.aspect.InternalOnly;
import org.tama.tamaapi.exception.ErrorMessageUtil;
import org.tama.tamaapi.domain.order.Order;
import org.tama.tamaapi.domain.order.OrderItem;
import org.tama.tamaapi.dto.feign.FullOrderResponse;
import org.tama.tamaapi.dto.feign.ItemOrderCountResponse;
import org.tama.tamaapi.query.order.OrderItemQueryRepository;
import org.tama.tamaapi.query.order.OrderQueryRepository;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.tama.tamaapi.exception.ErrorMessageUtil.NOT_FOUND_ORDER_ITEM;

@RestController
@RequiredArgsConstructor
@InternalOnly
public class FeignApiController {

    private final OrderItemQueryRepository orderItemQueryRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/orders/{orderId}/item")
    public List<ItemOrderCountResponse> getOrderItems(@PathVariable Long orderId) {
        //내부 api라 본인 인증 생략
        /*
        if(memberId == null)
            throw new IllegalArgumentException("인증되지 않았습니다");

        Long orderMemberId = orderItems.get(0).getOrder().getMemberId();
        if(!memberId.equals(orderMemberId))
            throw new AuthorizationDeniedException(ErrorMessageUtil.ACCESS_DENIED);
         */

        List<OrderItem> orderItems = orderItemQueryRepository.findAllWithOrderByOrderId(orderId);
        List<ItemOrderCountResponse> itemOrderCountRespons = orderItems.stream().map(ItemOrderCountResponse::new).toList();
        return itemOrderCountRespons;
    }

    /*
    //Long memberId로 받으면, 토큰 인증 안한건데 개발자가 그냥 호출할 가능성 있으니 주의
    @GetMapping("/api/orders/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId, @AuthenticationPrincipal CustomPrincipal principal) {
        System.out.println("orderId = " + orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        System.out.println("order.getId() = " + order.getId());
        System.out.println("order.getMemberId() = " + order.getMemberId());
        //본인 인증 (근데 회원 PK만 보이는데 안해도 될 것 같기도 하고.. 유추는 되겠지만)
        Long memberId = order.getMemberId();
        System.out.println("principal = " + principal);

        if(!memberId.equals(principal.getMemberId()))
            throw new AuthorizationDeniedException(ErrorMessageUtil.ACCESS_DENIED);
        OrderResponse orderResponse = new OrderResponse(order);
        System.out.println("orderResponse = " + orderResponse);
        return orderResponse;
    }
    */

    //sql 조인용 msa가 사용
    @GetMapping("/api/orders/{orderId}/full")
    public FullOrderResponse getFullOrder(@PathVariable Long orderId) {
        //내부 API라 본인 인증 생략
        Order order = orderQueryRepository.findFullByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));

        return new FullOrderResponse(order);
    }

    @GetMapping("/api/ordersItem/{orderItemId}/member")
    public Long getOrderItemMember(@PathVariable Long orderItemId) {
        OrderItem orderItem = orderItemQueryRepository.findWithOrderById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER_ITEM));
        return orderItem.getOrder().getMemberId();
    }

    //존재하는 paymentId만 return
    @GetMapping("/api/orders/payment-ids/existing")
    public List<String> findExistingPaymentIds(@RequestParam List<String> paymentIds) {
        List<Order> orders = orderQueryRepository.findAllByPaymentIdIn(paymentIds);
        return orders.stream().map(Order::getPaymentId).toList();
    }

}