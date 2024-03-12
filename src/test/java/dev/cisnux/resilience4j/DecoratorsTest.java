package dev.cisnux.resilience4j;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
public class DecoratorsTest {
    @SneakyThrows
    void slow() {
        log.info("Slow");
        Thread.sleep(1000L);
        throw new IllegalArgumentException("Error");
    }

    @SneakyThrows
    String greeting() {
        log.info("Say hi!");
        Thread.sleep(1000L);
        throw new IllegalArgumentException("Error");
    }

    @Test
    void decorators() {
        final var rateLimiter = RateLimiter.of("cisnux-ratelimiter", RateLimiterConfig.custom()
                .limitForPeriod(7)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .build());
        final var retry = Retry.of("cisnux-retry", RetryConfig.custom()
                .maxAttempts(10)
                .waitDuration(Duration.ofMillis(10))
                .build());

        final var runnable = Decorators.ofRunnable(this::slow)
                .withRateLimiter(rateLimiter)
                .withRetry(retry);

        runnable.run();
    }

    @Test
    void fallback() {
        final var rateLimiter = RateLimiter.of("cisnux-ratelimiter", RateLimiterConfig.custom()
                .limitForPeriod(7)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .build());
        final var retry = Retry.of("cisnux-retry", RetryConfig.custom()
                .maxAttempts(10)
                .waitDuration(Duration.ofMillis(10))
                .build());

        final var suppliers = Decorators.ofSupplier(this::greeting)
                .withRateLimiter(rateLimiter)
                .withRetry(retry)
                .withFallback((s, throwable) -> "Stopped: " + throwable.getMessage());

        System.out.println(suppliers.get());
    }
}
