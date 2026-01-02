package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.command.EmailService;
import org.example.tamaapi.command.PortOneService;
import org.example.tamaapi.command.order.OrderService;
import org.example.tamaapi.common.auth.CustomPrincipal;
import org.example.tamaapi.common.exception.MyBadRequestException;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.requestDto.order.CancelMemberOrderRequest;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.feignClient.item.ItemFeignClient;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.example.tamaapi.query.order.OrderQueryRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.example.tamaapi.common.util.ErrorMessageUtil.INVALID_HEADER;
import static org.example.tamaapi.common.util.ErrorMessageUtil.NOT_FOUND_ORDER;

;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestApiController {

    private final OrderService orderService;
    private final OrderQueryRepository orderQueryRepository;
    private final EmailService emailService;
    private final PortOneService portOneService;
    private final ItemFeignClient itemFeignClient;
    //private final OrderEventProducer orderEventProducer;

    @PostMapping("/api/test/circuit-breaker")
    public ResponseEntity<SimpleResponse> saveMemberOrder(@AuthenticationPrincipal CustomPrincipal principal
            ,@RequestBody @Valid OrderRequest req) {
        Long memberId = principal.getMemberId();

        List<ItemOrderCountRequest> requests = req.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();

        int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
        orderService.validateMemberFreeOrderPrice(orderItemsPrice, req.getMemberCouponId(), req.getUsedPoint(), memberId);

        orderService.saveMemberFreeOrder(
                memberId,
                req.getReceiverNickname(),
                req.getReceiverPhone(),
                req.getZipCode(),
                req.getStreetAddress(),
                req.getDetailAddress(),
                req.getDeliveryMessage(),
                req.getMemberCouponId(),
                req.getUsedPoint(),
                req.getOrderItems(),
                principal.getBearerJwt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
    }

}
