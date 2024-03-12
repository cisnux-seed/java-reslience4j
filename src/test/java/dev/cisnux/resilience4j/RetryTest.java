package dev.cisnux.resilience4j;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class RetryTest {
    void callMe() {
        log.info("processing ...");
        throw new IllegalArgumentException("it shouldn't be accessed");
    }

    @Test
    void createNewRetry() {
        final var retry = Retry.ofDefaults("cisnux");
        final var runnable = Retry.decorateRunnable(retry, this::callMe);
        runnable.run();
    }

    @Test
    void createRetrySupplier() {
        final var retry = Retry.ofDefaults("cisnux");
        final var supplier = Retry.decorateSupplier(retry, () -> greeting("cisnux"));
        supplier.get();
    }

    String greeting(String name) {
        log.info("processing {} ...", name);
        throw new IllegalArgumentException("it shouldn't be accessed");
    }
}
