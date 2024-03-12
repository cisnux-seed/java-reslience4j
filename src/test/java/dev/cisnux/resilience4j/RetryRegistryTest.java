package dev.cisnux.resilience4j;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
public class RetryRegistryTest {
    void callMe() {
        log.info("processing ...");
        throw new IllegalArgumentException("it shouldn't be accessed");
    }

    @Test
    void testRetryRegistry() {
        final var registry = RetryRegistry.ofDefaults();
        final var retry1 = registry.retry("cisnux");
        final var retry2 = registry.retry("cisnux");

        Assertions.assertSame(retry1, retry2);
    }

    @Test
    void testRetryRegistryConfig() {
        final var config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(30))
                .build();
        final var registry = RetryRegistry.ofDefaults();
        registry.addConfiguration("cisnux_config", config);
        final var retry = registry.retry("cisnux", "cisnux_config");
        retry.executeRunnable(this::callMe);
    }
}
