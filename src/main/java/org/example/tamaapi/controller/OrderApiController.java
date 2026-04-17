package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.common.auth.CustomPrincipal;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.feign.ItemOrderCountRequestWrapper;
import org.example.tamaapi.dto.requestDto.order.*;;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.common.exception.MyBadRequestException;
import org.example.tamaapi.event.OrderEventProducer;
import org.example.tamaapi.feignClient.item.ItemFeignClient;
import org.example.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.example.tamaapi.query.order.OrderQueryRepository;

import org.example.tamaapi.command.EmailService;
import org.example.tamaapi.command.order.OrderService;
import org.example.tamaapi.command.PortOneService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.example.tamaapi.common.util.ErrorMessageUtil.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final OrderService orderService;
    private final OrderQueryRepository orderQueryRepository;
    private final PortOneService portOneService;
    private final EmailService emailService;
    private final ItemFeignClient itemFeignClient;
    private final OrderEventProducer orderEventProducer;

    @PostMapping("/api/orders/member")
    public ResponseEntity<SimpleResponse> saveMemberOrder(@RequestParam String paymentId, @AuthenticationPrincipal Long memberId) {
        Map<String, Object> paymentResponse = portOneService.findByPaymentId(paymentId);
        PortOnePaymentStatus paymentStatus = PortOnePaymentStatus.valueOf((String) paymentResponse.get("status"));
        PortOneOrder portOneOrder = portOneService.convertCustomData((String) paymentResponse.get("customData"), paymentId);
        int clientTotal = (int) ((Map<String, Object>) paymentResponse.get("amount")).get("total");
        List<ItemOrderCountRequest> requests = portOneOrder.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();
        int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
        portOneService.validatePaymentStatus(paymentStatus);
        orderService.validateGuestOrder(orderItemsPrice, portOneOrder, clientTotal);

        orderService.validateMemberOrder(orderItemsPrice, portOneOrder, clientTotal, memberId);
        orderService.saveMemberOrder(
                orderItemsPrice,
                portOneOrder.getPaymentId(),
                memberId,
                portOneOrder.getReceiverNickname(),
                portOneOrder.getReceiverPhone(),
                portOneOrder.getZipCode(),
                portOneOrder.getStreetAddress(),
                portOneOrder.getDetailAddress(),
                portOneOrder.getDeliveryMessage(),
                portOneOrder.getMemberCouponId(),
                portOneOrder.getUsedPoint(),
                portOneOrder.getOrderItems());

        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
    }

    //멤버 무료 주문 저장
    //비회원은 쿠폰, 포인트 없어서 무료 주문 불가 -> 비회원용 API 안 만듬
    //포트원을 거치지 않음 -> 리다이렉트 X -> 모바일용 API 안 만듬
    @PostMapping("/api/orders/free/member")
    public ResponseEntity<SimpleResponse> saveMemberOrder(@RequestBody @Valid FreeOrderRequest req, @AuthenticationPrincipal Long memberId) {
        List<ItemOrderCountRequest> requests = req.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();
        int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
        orderService.validateMemberFreeOrderPrice(orderItemsPrice, req.getMemberCouponId(), req.getUsedPoint(), memberId);
        orderService.saveMemberFreeOrder(
                orderItemsPrice,
                memberId,
                req.getReceiverNickname(),
                req.getReceiverPhone(),
                req.getZipCode(),
                req.getStreetAddress(),
                req.getDetailAddress(),
                req.getDeliveryMessage(),
                req.getMemberCouponId(),
                req.getUsedPoint(),
                req.getOrderItems()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
    }

    //비로그인 주문 저장
    @PostMapping("/api/orders/guest")
    //@LogExecutionTime
    public ResponseEntity<SimpleResponse> saveGuestOrder(@RequestParam String paymentId) {
        Map<String, Object> paymentResponse = portOneService.findByPaymentId(paymentId);
        PortOnePaymentStatus paymentStatus = PortOnePaymentStatus.valueOf((String) paymentResponse.get("status"));
        PortOneOrder portOneOrder = portOneService.convertCustomData((String) paymentResponse.get("customData"), paymentId);
        int clientTotal = (int) ((Map<String, Object>) paymentResponse.get("amount")).get("total");
        List<ItemOrderCountRequest> requests = portOneOrder.getOrderItems().stream().map(ItemOrderCountRequest::new).toList();
        int orderItemsPrice = itemFeignClient.getTotalPrice(requests);
        portOneService.validatePaymentStatus(paymentStatus);
        orderService.validateGuestOrder(orderItemsPrice, portOneOrder, clientTotal);

        Long orderId = orderService.saveGuestOrder(
                orderItemsPrice,
                portOneOrder.getPaymentId(),
                portOneOrder.getSenderNickname(),
                portOneOrder.getSenderEmail(),
                portOneOrder.getReceiverNickname(),
                portOneOrder.getReceiverPhone(),
                portOneOrder.getZipCode(),
                portOneOrder.getStreetAddress(),
                portOneOrder.getDetailAddress(),
                portOneOrder.getDeliveryMessage(),
                portOneOrder.getOrderItems()
        );

        emailService.sendGuestOrderEmailAsync(portOneOrder.getSenderEmail(), portOneOrder.getSenderNickname(), orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
    }

    //멤버 주문 취소 접수
    @PutMapping("/api/orders/member/cancel/received")
    public ResponseEntity<SimpleResponse> cancelReceivedMemberOrder(@Valid @RequestBody CancelMemberOrderRequest req, @AuthenticationPrincipal Long memberId) {
        if (memberId == null)
            throw new MyBadRequestException("액세스 토큰이 비었습니다.");

        orderService.receiveCancelMemberOrder(req.getOrderId(), memberId, req.getReason());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("주문 취소 접수 완료"));
    }

    //게스트 주문 취소 접수
    @PutMapping("/api/orders/guest/cancel/received")
    public ResponseEntity<SimpleResponse> cancelReceivedGuestOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @AuthenticationPrincipal Long memberId) {
        // "Basic YWRtaW46cGFzc3dvcmQ=" 형태 → Base64 디코딩
        if (authHeader == null || !authHeader.startsWith("Basic "))
            throw new IllegalArgumentException(INVALID_HEADER);

        String base64Credentials = authHeader.substring(6); // "Basic " 이후의 값 추출
        String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        // "orderId:buyerName" 형태에서 분리
        String[] values = decodedCredentials.split(":", 2);
        if (values.length != 2)
            throw new IllegalArgumentException(INVALID_HEADER);

        String buyerName = values[0];
        Long orderId = Long.parseLong(values[1]);

        orderService.receiveCancelGuestOrder(orderId, memberId,  buyerName, "구매자 취소 요청");
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("주문 취소 접수 완료"));
    }

    //localhost는 webhook 못씀
    //결제가 되면 포트원이 tama 엔드포인트 호출. 즉 리엑트에서 호출하는게 아니므로 포트원 결제 내역에 필요한 주문 정보를 다 저장해야함
    //webhook은 통신 질이 좋아지지만, 포트원이 DB 수준으로 정보를 갖게 됨 -> 팀원이랑 상의 필요
    //모바일 결제는 리다이렉트 방식이라 webhook처럼 포트원에 정보를 저장해야함
    //webhook url은 하나만 가능, 로직 완료시 클라이언트 응답 불가 -> 웹훅 포기
    @PostMapping("/api/webhook/portOne")
    public void webhook() {

    }

}
