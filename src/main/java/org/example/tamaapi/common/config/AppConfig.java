package org.example.tamaapi.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import feign.FeignException;
import feign.RequestInterceptor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.common.exception.OrderFailException;
import org.example.tamaapi.common.exception.feign.MemberFeignCommandException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    //LocalDate 직렬화 용도
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        return new Jackson2ObjectMapperBuilder()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(javaTimeModule)
                .timeZone("Asia/Seoul")
                .build();
    }

    //Response Json xss 방지
    @Bean
    public MappingJackson2HttpMessageConverter jsonEscapeConverter() {
        ObjectMapper copy = objectMapper().copy();
        copy.getFactory().setCharacterEscapes(new HtmlCharacterEscapes());
        return new MappingJackson2HttpMessageConverter(copy);
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em){
        return new JPAQueryFactory(em);
    }

    /*
    @Bean
    public CircuitBreakerConfig defaultFeignCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .recordExceptions(MemberFeignCommandException.class)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            CircuitBreakerConfig defaultFeignCircuitBreakerConfig) {

        CircuitBreakerRegistry registry =
                CircuitBreakerRegistry.of(defaultFeignCircuitBreakerConfig);

        registry.circuitBreaker("itemService");
        registry.circuitBreaker("memberService");

        return registry;
    }
    */

    /*
    @Bean
    public RequestInterceptor requestLoggingInterceptor() {
        return template -> log.info("[Feign Request] method={}, url={}, query={}, header={} , body={}",
                template.method(), template.url(), template.queryLine(), template.headers(), (template.body() != null) ? new String(template.body()) : "null");
    }
     */

    @Bean
    public ApplicationRunner circuitBreakerLogger(CircuitBreakerRegistry registry) {
        return args -> registry.getAllCircuitBreakers()
                .forEach(this::registerEventLogger);
    }

    private void registerEventLogger(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("[CB-STATE] name={} {} -> {}",
                                circuitBreaker.getName(),
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()
                        )
                )
                .onFailureRateExceeded(event ->
                        log.warn("[CB-FAILURE-RATE] name={} rate={}%",
                                circuitBreaker.getName(),
                                event.getFailureRate()
                        )
                )
                .onCallNotPermitted(event ->
                        log.error("[CB-BLOCKED] name={} call not permitted",
                                circuitBreaker.getName()
                        )
                )
                .onError(event ->
                        log.error("[CB-ERROR] name={} error={}",
                                circuitBreaker.getName(),
                                event.getThrowable().toString()
                        )
                )
                .onSuccess(event ->
                        log.debug("[CB-SUCCESS] name={} duration={}ms",
                                circuitBreaker.getName(),
                                event.getElapsedDuration().toMillis()
                        )
                );
    }

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int CONNECTION_TIMEOUT_SECONDS = 1;
        int READ_TIMEOUT_SECONDS = 5;
        requestFactory.setConnectTimeout(CONNECTION_TIMEOUT_SECONDS);
        requestFactory.setReadTimeout(READ_TIMEOUT_SECONDS);
        return restClientBuilder
                .requestFactory(requestFactory)
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            log.error("HTTP request failed.");
                            log.error("Request: {} {}", request.getMethod(), request.getURI());
                            log.error("Response: {} {}", response.getStatusCode(), response.getStatusText());
                            throw new RuntimeException(response.getStatusText());
                        }
                )
                .build();
    }
}