package dev.cisnux.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CircuitBreakerTest {
    private final AtomicLong counter = new AtomicLong(0L);

    void callMe() {
        log.info("Call me");
        counter.incrementAndGet();
        log.info("value {}", counter.get());
        throw new IllegalArgumentException("Ups");
    }

    @Test
    void circuitBreaker() {
        final var circuitBreaker = CircuitBreaker.ofDefaults("cisnux");

        for (int i = 0; i < 200; i++) {
            try {
                final var runnable = CircuitBreaker.decorateRunnable(circuitBreaker, this::callMe);
                runnable.run();
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
            }
        }
    }

    @Test
    void circuitBreakerConfig() {
        final var config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(10f)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(10)
                .build();
        final var circuitBreaker = CircuitBreaker.of("cisnux", config);

        for (int i = 0; i < 200; i++) {
            try {
                final var runnable = CircuitBreaker.decorateRunnable(circuitBreaker, this::callMe);
                runnable.run();
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
            }
        }
    }

    @Test
    void circuitBreakerRegistry() {
        final var config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(10f)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(10)
                .build();
        final var registry = CircuitBreakerRegistry.ofDefaults();
        final var circuitBreaker = registry.circuitBreaker("cisnux", config);

        for (int i = 0; i < 200; i++) {
            try {
                final var runnable = CircuitBreaker.decorateRunnable(circuitBreaker, this::callMe);
                runnable.run();
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
            }
        }
    }
}
