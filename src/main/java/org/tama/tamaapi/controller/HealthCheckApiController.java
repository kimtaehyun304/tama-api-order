package org.tama.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tama.sharelib.common.dto.SimpleResponse;
import org.tama.sharelib.common.exception.CustomBadRequestException;
import org.tama.tamaapi.command.EmailService;
import org.tama.tamaapi.command.PortOneService;
import org.tama.tamaapi.command.order.OrderService;
import org.tama.tamaapi.domain.order.PortOnePaymentStatus;
import org.tama.tamaapi.dto.PortOneOrder;
import org.tama.tamaapi.dto.requestDto.order.CancelMemberOrderRequest;
import org.tama.tamaapi.dto.requestDto.order.FreeOrderRequest;
import org.tama.tamaapi.event.OrderEventProducer;
import org.tama.tamaapi.exception.ErrorMessageUtil;
import org.tama.tamaapi.feignClient.item.ItemFeignClient;
import org.tama.tamaapi.feignClient.item.ItemOrderCountRequest;
import org.tama.tamaapi.query.order.OrderQueryRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthCheckApiController {


    @GetMapping("/api/orders/health-check/ok")
    public String healthOk() {
        log.info("ok");
        return "ok";
    }

    @GetMapping("/api/orders/health-check/error")
    public String healthError() {
        log.error("error");
        throw new RuntimeException("error");
    }

    @GetMapping("/api/orders/health-check/warn")
    public String healthWarn() {
        log.warn("warn");
        return "warn";
    }

    @GetMapping("/api/orders/health-check/throw")
    public String throwException() {
        throw new RuntimeException("error");
    }
}
