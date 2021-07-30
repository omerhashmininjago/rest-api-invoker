package com.omer.util.rest;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
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
 * A retry generic implementation to invoke endpoints exposed by services
 * This uses a Circuit Breaker implementation provided by the Resilience4j library
 *
 * </p>
 *
 * @param <T> T Represents the expected response i.e. class type to be returned
 *            wrapped in ResponseEntity
 */
public interface RetryRestInvoker<T> extends RestInvoker<T> {

    default ResponseEntity<T> invoke(@NonNull String url, @NonNull String endPoint, @NonNull HttpMethod httpMethod, @NonNull HttpHeaders httpHeaders, @NonNull String requestBody, @NonNull Class<T> classReference) {
        return RetryUtil.invoke(url + endPoint, httpMethod, new HttpEntity<>(requestBody, httpHeaders), classReference);
    }

    @Component
    class RetryUtil {

        @Value("${maxAttempts: 3}")
        private int maxAttempts;
        @Value("${intervalFunction: 500}")
        private Long intervalFunction;
        @Value("${waitDuration: 30}")
        private Long waitDuration;

        private static Retry RETRY;

        @PostConstruct
        private void init() {
            RETRY = RetryRegistry.of(RetryConfig
                    .custom()
                            .intervalFunction(IntervalFunction.of(this.intervalFunction))
                            .maxAttempts(this.maxAttempts)
                            .waitDuration(Duration.ofSeconds(this.waitDuration))
                    .build()).retry("retry");
        }

        public static <T> ResponseEntity<T> invoke(@NonNull String url, @NonNull HttpMethod httpMethod, @NonNull HttpEntity httpEntity, @NonNull Class classReference) {
            Supplier<ResponseEntity> decoratedSupplier = Retry.decorateSupplier(RETRY, invokeEndPoint(url, httpMethod, httpEntity, classReference));

            return Try.ofSupplier(decoratedSupplier)
                    .recover(throwable -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()).get();
        }

        private static Supplier<ResponseEntity> invokeEndPoint(@NonNull String url, @NonNull HttpMethod httpMethod, @NonNull HttpEntity httpEntity, @NonNull Class classReference) {
            return () -> REST_TEMPLATE.exchange(url, httpMethod, httpEntity, classReference);
        }
    }
}
