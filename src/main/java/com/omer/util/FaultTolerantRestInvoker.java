package com.omer.util;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * <p>
 * A fault tolerant, generic implementation to invoke endpoints exposed by services
 * This uses a Circuit Breaker implementation provided by the Resilience4j library
 *
 * The Implementation class can override the properties of the CircuitBreakerConfiguration
 * based on their needs
 * </p>
 *
 * @param <T> T Represents the expected response i.e. class type to be returned
 *            wrapped in ResponseEntity
 */
public interface FaultTolerantRestInvoker<T> extends RestInvoker<T> {

    default ResponseEntity<T> invoke(@NonNull String url, @NonNull String endPoint, @NonNull HttpMethod httpMethod, @NonNull HttpHeaders httpHeaders, @NonNull String requestBody, @NonNull Class<T> classReference) {
        return FaultTolerantUtil.invoke(url + endPoint, httpMethod, new HttpEntity<>(requestBody, httpHeaders), classReference);
    }

    @Component
    class FaultTolerantUtil {

        @Value("${automaticTransitionFromOpenToHalfOpenEnabled: false}")
        private boolean automaticTransitionFromOpenToHalfOpenEnabled;
        @Value("${failureRateThreshold: 50.0F}")
        private Float failureRateThreshold;
        @Value("${minimumNumberOfCalls: 100}")
        private int minimumNumberOfCalls;
        @Value("${writableStackTraceEnabled: true}")
        private boolean writableStackTraceEnabled;
        @Value("${permittedNumberOfCallsInHalfOpenState: 10}")
        private int permittedNumberOfCallsInHalfOpenState;
        @Value("${slidingWindowSize: 100}")
        private int slidingWindowSize;
        @Value("${waitIntervalFunctionInOpenState: 60L}")
        private Long waitIntervalFunctionInOpenState;
        @Value("${slowCallRateThreshold: 100.0F}")
        private Float slowCallRateThreshold;
        @Value("${slowCallDurationThreshold: 60L}")
        private Long slowCallDurationThreshold;
        @Value("${maxWaitDurationInHalfOpenState: 0L}")
        private Long maxWaitDurationInHalfOpenState;

        private static CircuitBreaker FAULT_TOLERANCE;

        @PostConstruct
        private void init() {
            FAULT_TOLERANCE = CircuitBreakerRegistry
                    .of(CircuitBreakerConfig
                            .custom()
                            .automaticTransitionFromOpenToHalfOpenEnabled(this.automaticTransitionFromOpenToHalfOpenEnabled)
                            .maxWaitDurationInHalfOpenState(Duration.ofSeconds(this.maxWaitDurationInHalfOpenState))
                            .slowCallDurationThreshold(Duration.ofSeconds(this.slowCallDurationThreshold))
                            .slowCallRateThreshold(this.slowCallRateThreshold)
                            .waitIntervalFunctionInOpenState(IntervalFunction.of(Duration.ofSeconds(this.waitIntervalFunctionInOpenState)))
                            .failureRateThreshold(this.failureRateThreshold)
                            .minimumNumberOfCalls(this.minimumNumberOfCalls)
                            .writableStackTraceEnabled(this.writableStackTraceEnabled)
                            .permittedNumberOfCallsInHalfOpenState(this.permittedNumberOfCallsInHalfOpenState)
                            .slidingWindowSize(this.slidingWindowSize)
                            .enableAutomaticTransitionFromOpenToHalfOpen()
                            .build())
                    .circuitBreaker("faultTolerant");

        }

        public static <T> ResponseEntity<T> invoke(@NonNull String url, @NonNull HttpMethod httpMethod, @NonNull HttpEntity httpEntity, @NonNull Class classReference) {
            Supplier<ResponseEntity> decoratedSupplier = CircuitBreaker.decorateSupplier(FAULT_TOLERANCE, invokeEndPoint(url, httpMethod, httpEntity, classReference));

            return Try.ofSupplier(decoratedSupplier)
                    .recover(throwable -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()).get();
        }

        private static Supplier<ResponseEntity> invokeEndPoint(@NonNull String url, @NonNull HttpMethod httpMethod, @NonNull HttpEntity httpEntity, @NonNull Class classReference) {
            return () -> REST_TEMPLATE.exchange(url, httpMethod, httpEntity, classReference);
        }
    }
}
