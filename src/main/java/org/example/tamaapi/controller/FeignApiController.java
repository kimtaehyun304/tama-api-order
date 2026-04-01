package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.command.EmailService;
import org.example.tamaapi.command.PortOneService;
import org.example.tamaapi.command.order.OrderService;
import org.example.tamaapi.common.aspect.InternalOnly;
import org.example.tamaapi.common.auth.CustomPrincipal;
import org.example.tamaapi.common.util.ErrorMessageUtil;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.feign.FullOrderResponse;
import org.example.tamaapi.dto.feign.ItemOrderCountResponse;
import org.example.tamaapi.dto.requestDto.order.OrderDetailRequest;
import org.example.tamaapi.dto.requestDto.order.FreeOrderRequest;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.feignClient.item.ItemFeignClient;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.example.tamaapi.query.order.OrderItemQueryRepository;
import org.example.tamaapi.query.order.OrderQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import static org.example.tamaapi.common.util.ErrorMessageUtil.NOT_FOUND_ORDER;
import static org.example.tamaapi.common.util.ErrorMessageUtil.NOT_FOUND_ORDER_ITEM;

@RestController
@RequiredArgsConstructor
@InternalOnly
public class FeignApiController {

    private final OrderItemQueryRepository orderItemQueryRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/orders/{orderId}/item")
    public List<ItemOrderCountResponse> getOrderItems(@PathVariable Long orderId, @AuthenticationPrincipal CustomPrincipal principal) {
        List<OrderItem> orderItems = orderItemQueryRepository.findAllWithOrderByOrderId(orderId);

        //본인 인증
        Long memberId = orderItems.get(0).getOrder().getMemberId();
        if(!memberId.equals(principal.getMemberId()))
            throw new AuthorizationDeniedException(ErrorMessageUtil.ACCESS_DENIED);

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

    //카프카가 사용하는 API
    @GetMapping("/api/orders/{orderId}/full")
    public FullOrderResponse getFullOrder(@PathVariable Long orderId) {
        Order order = orderQueryRepository.findFullByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));

        //내부 API라 본인 인증 생략 (할순 있는데 카프카에 JWT 담기엔 부하가 걱정되서 생략)
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