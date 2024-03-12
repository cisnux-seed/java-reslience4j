package dev.cisnux.resilience4j;

import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class MetricTest {
    @Test
    void retry() {
        final var retry = Retry.ofDefaults("cisnux");

        try {
            final var supplier = Retry.decorateSupplier(retry, this::greeting);
            supplier.get();
        } catch (Exception e) {
            System.out.println(retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
            System.out.println(retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
            System.out.println(retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
            System.out.println(retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
        }
    }

    @SneakyThrows
    String greeting() {
        log.info("Say hi!");
        Thread.sleep(1000L);
        throw new IllegalArgumentException("Error");
    }
}
