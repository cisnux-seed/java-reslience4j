package dev.cisnux.resilience4j;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class EventPublisherTest {
    @Test
    void retry() {
        final var retry = Retry.ofDefaults("cisnux");
        retry.getEventPublisher()
                .onError(event -> System.out.println("error " + event.getName()))
                .onSuccess(event -> System.out.println("nothing"))
                .onRetry(event -> System.out.println("event " + event.getName()));

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

    @Test
    void registry() {
        final var registry = RetryRegistry.ofDefaults();
        registry.getEventPublisher().onEntryAdded(event -> System.out.println("added\t: " + event.getAddedEntry().getName()));

        registry.retry("cisnux");
        registry.retry("predator");
        registry.retry("cisnux-preadtor");
    }

    @SneakyThrows
    String greeting() {
        log.info("Say hi!");
        Thread.sleep(1000L);
        throw new IllegalArgumentException("Error");
    }
}
