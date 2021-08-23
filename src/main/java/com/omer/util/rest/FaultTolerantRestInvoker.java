package com.omer.util.rest;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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
public final class FaultTolerantRestInvoker<T> extends RestInvoker<T> {

    protected ResponseEntity<T> invoke(@NonNull final String uri, @NonNull final HttpMethod httpMethod, @NonNull final HttpHeaders httpHeaders, @NonNull final String requestBody, @NonNull final Class<T> classReference) {
        return FaultTolerantUtil.invoke(uri, httpMethod, new HttpEntity<>(requestBody, httpHeaders), classReference);
    }

    @Component
    private static class FaultTolerantUtil {

        @Value("${automaticTransitionFromOpenToHalfOpenEnabled: false}")
        private boolean automaticTransitionFromOpenToHalfOpenEnabled;
        @Value("${failureRateThreshold: 50.0}")
        private Float failureRateThreshold;
        @Value("${minimumNumberOfCalls: 100}")
        private int minimumNumberOfCalls;
        @Value("${writableStackTraceEnabled: true}")
        private boolean writableStackTraceEnabled;
        @Value("${permittedNumberOfCallsInHalfOpenState: 10}")
        private int permittedNumberOfCallsInHalfOpenState;
        @Value("${slidingWindowSize: 100}")
        private int slidingWindowSize;
        @Value("${waitIntervalFunctionInOpenState: 60}")
        private Long waitIntervalFunctionInOpenState;
        @Value("${slowCallRateThreshold: 100.0}")
        private Float slowCallRateThreshold;
        @Value("${slowCallDurationThreshold: 60}")
        private Long slowCallDurationThreshold;
        @Value("${maxWaitDurationInHalfOpenState: 0}")
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

        public static <T> ResponseEntity<T> invoke(@NonNull final String url, @NonNull final HttpMethod httpMethod, @NonNull final HttpEntity httpEntity, @NonNull final Class classReference) {
            Supplier<ResponseEntity> decoratedSupplier = CircuitBreaker.decorateSupplier(FAULT_TOLERANCE, invokeEndPoint(url, httpMethod, httpEntity, classReference));

            return Try.ofSupplier(decoratedSupplier)
                    .recover(throwable -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()).get();
        }

        private static Supplier<ResponseEntity> invokeEndPoint(@NonNull final String url, @NonNull final HttpMethod httpMethod, @NonNull final HttpEntity httpEntity, @NonNull final Class classReference) {
            return () -> REST_TEMPLATE.exchange(url, httpMethod, httpEntity, classReference);
        }
    }
}
