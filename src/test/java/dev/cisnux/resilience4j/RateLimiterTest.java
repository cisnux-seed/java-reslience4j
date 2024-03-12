package dev.cisnux.resilience4j;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RateLimiterTest {
    private final AtomicLong counter = new AtomicLong(0L);

    @Test
    void testRateLimiter() {
        final var rateLimiter = RateLimiter.ofDefaults("cisnux");

        for (int i = 0; i < 10_000; i++) {
            rateLimiter.executeRunnable(() -> {
                final var result = counter.incrementAndGet();
                log.info("Result: {}", result);
            });
        }
    }

    @Test
    void testRateLimiterConfig() {
        final var config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(2))
                .build();
        final var rateLimiter = RateLimiter.of("cisnux", config);

        for (int i = 0; i < 10_000; i++) {
            rateLimiter.executeRunnable(() -> {
                try {
                    Thread.sleep(500L);
                    final var result = counter.incrementAndGet();
                    log.info("Result: {}", result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    void testRateLimiterOtherConfig() {
        final var config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                // will refresh after 1 minutes
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofMinutes(1))
                .build();
        final var rateLimiter = RateLimiter.of("cisnux", config);

        for (int i = 0; i < 500; i++) {
            rateLimiter.executeRunnable(() -> {
                try {
                    Thread.sleep(500L);
                    final var result = counter.incrementAndGet();
                    log.info("Result: {}", result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    void testRateLimiterRegistry() {
        final var config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(2))
                .build();
        final var registry = RateLimiterRegistry.ofDefaults();
        registry.addConfiguration("cisnux_rate_limiter_config", config);

        final var rateLimiter1 = registry.rateLimiter("cisnux", "cisnux_rate_limiter_config");
        final var rateLimiter2 = registry.rateLimiter("cisnux", "cisnux_rate_limiter_config");

        Assertions.assertSame(rateLimiter1, rateLimiter2);

        for (int i = 0; i < 10_000; i++) {
            rateLimiter1.executeRunnable(() -> {
                final var result = counter.incrementAndGet();
                log.info("Result: {}", result);
            });
        }
    }
}
