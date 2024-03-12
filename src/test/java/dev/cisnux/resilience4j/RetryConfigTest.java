package dev.cisnux.resilience4j;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
public class RetryConfigTest {
    String greeting(String name) {
        log.info("processing {} ...", name);
        throw new IllegalArgumentException("it shouldn't be accessed");
    }

    @Test
    void retryConfig() {
        final var config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(30))
                .build();
        final var retry = Retry.of("cisnux", config);
        final var supplier = Retry.decorateSupplier(retry, () -> greeting("cisnux"));
        supplier.get();
    }

    @Test
    void otherRetryConfig() {
        final var config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(2))
                // if the exception is IllegalArgumentException
                // then it wouldn't retry
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        final var retry = Retry.of("cisnux", config);
        final var supplier = Retry.decorateSupplier(retry, () -> greeting("cisnux"));
        supplier.get();
    }
}
