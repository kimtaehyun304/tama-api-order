package org.example.tamaapi.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.common.exception.OrderFailException;
import org.example.tamaapi.common.exception.WillCancelPaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PortOneService {

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;
    private final ObjectMapper objectMapper;


    //결제내역 단건 조회
    public Map<String, Object> findByPaymentId(String paymentId) {
        return RestClient.create().get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    //res 포함하면 복잡해서 포함 안했음. 보안상 민감한 내용 있을수도 있고
                    String clientMsg = String.format("결제 검증 중 오류가 발생했습니다. 원인: 포트원 결제내역 단건조회 API 호출 실패");
                    String serverMsg = String.format("결제 검증 중 오류가 발생했습니다. 원인: 포트원 결제내역 단건조회 API 호출 실패, res=%s", res);
                    log.error(serverMsg);
                    throw new IllegalArgumentException(clientMsg);
                })
                .body(new ParameterizedTypeReference<>() {
                });

    }

    public void cancelPayment(String paymentId, String reason) {
        RestClient.create().post()
                .uri("https://api.portone.io/payments/{paymentId}/cancel", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .body(Map.of("reason", reason)) // 문자열로 JSON 전달
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    //res 포함하면 복잡해서 포함 안했음. 보안상 민감한 내용 있을수도 있고
                    String clientMsg = String.format("결제 취소를 실패했습니다. 원인: 포트원 결제 취소 API 호출 실패");

                    String serverMsg = String.format("결제 취소를 실패했습니다. 원인: 포트원 결제 취소 API 호출 실패, res:%s", res);
                    log.error(serverMsg);
                    throw new IllegalArgumentException(clientMsg);
                })
                .toBodilessEntity();
        //예외로 인해 결제가 자동 취소되는 경우가 있는데, 이 때 잘 취소됐는지 확인을 위해
        log.debug(String.format("결제가 취소됐습니다. 이유:%s, 결제번호:%s", reason, paymentId));
    }

    //프론트에서 customData 양식 못 맞추면 예외 발생 가능
    public PortOneOrder convertCustomData(String customData, String paymentId) {

        try {
            PortOneOrder portOneOrder = objectMapper.readValue(customData, PortOneOrder.class);
            validateNotBlank(portOneOrder);
            return portOneOrder;
        } catch (JsonProcessingException e) {
            String clientMsg = "주문을 실패했습니다. 원인: PG사 데이터 직렬화 중 오류 발생";
            log.error(String.format("%s, customData:%s, message:%s", clientMsg, customData, e.getMessage()));
            cancelPayment(paymentId, clientMsg);
            throw new WillCancelPaymentException(clientMsg);
        }
        catch (Exception e) {
            String clientMsg = String.format("주문을 실패했습니다. 원인:%s", e.getMessage());
            log.error(clientMsg);
            cancelPayment(paymentId, clientMsg);
            throw new WillCancelPaymentException(clientMsg);
        }
    }

    //어짜피 결제 실패했기 때문에, 결제 취소 안해도 됨
    public void validatePaymentStatus(PortOnePaymentStatus paymentStatus) {
        if (!paymentStatus.equals(PortOnePaymentStatus.PAID))
            throw new OrderFailException("결제가 완료되지 않아서 주문을 진행할 수 없습니다");
    }

    private void validateNotBlank(PortOneOrder portOneOrder) {
        // 1. SaveOrderRequest 필드 검사
        for (Field field : PortOneOrder.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(portOneOrder);

                // String 빈 값 체크
                //쿠폰은 안 사용해도 정상이라 검사 제외
                if (!field.getName().equals("memberCouponId") &&  value == null || (value instanceof String && !StringUtils.hasText((String) value)))
                    throw new OrderFailException(String.format("[%s] 값 누락", field.getName()));

                // List 내부 검사
                if (value instanceof List<?> list) {
                    for (int i = 0; i < list.size(); i++) {
                        Object element = list.get(i);
                        if (element == null) {
                            throw new OrderFailException(String.format("[%s][%d] 값 누락", field.getName(), i));
                        }

                        // element 필드 검사 (SaveOrderItemRequest)
                        for (Field itemField : element.getClass().getDeclaredFields()) {
                            itemField.setAccessible(true);
                            Object itemValue = itemField.get(element);

                            if (itemValue == null || (itemValue instanceof String && !StringUtils.hasText((String) itemValue))) {
                                throw new OrderFailException(
                                        String.format("[%s][%d].%s 값 누락", field.getName(), i, itemField.getName())
                                );
                            }
                        }
                    }
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
