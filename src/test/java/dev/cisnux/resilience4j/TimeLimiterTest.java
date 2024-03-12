package dev.cisnux.resilience4j;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@Slf4j
public class TimeLimiterTest {

    @SneakyThrows
    String slow() {
        log.info("Slow");
        Thread.sleep(5000L);
        return "Cisnux";
    }

    @SneakyThrows
    @Test
    void timeLimiter() {
        @Cleanup final var executorService = Executors.newSingleThreadExecutor();
        final var future = executorService.submit(this::slow);
        // success
//        System.out.println(future.get());
        final var timeLimiter = TimeLimiter.ofDefaults("cisnux");
        final var callable = timeLimiter.decorateFutureSupplier(() -> future);
//        callable.call();
        // success if the time under 1 second (1000 milliseconds)
//        System.out.println(callable.call());
        Assertions.assertThrows(TimeoutException.class, callable::call);
    }

    @SneakyThrows
    @Test
    void timeLimiterConfig() {
        final var config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();
        @Cleanup final var executorService = Executors.newSingleThreadExecutor();
        final var future = executorService.submit(this::slow);
        // success
//        System.out.println(future.get());
        final var timeLimiter = TimeLimiter.of("cisnux", config);
        final var callable = timeLimiter.decorateFutureSupplier(() -> future);
//        callable.call();
        // success if the time under 1 second (1000 milliseconds)
//        System.out.println(callable.call());
        Assertions.assertDoesNotThrow(callable::call);
        System.out.println(callable.call());
    }

    @SneakyThrows
    @Test
    void timeLimiterRegistry() {
        final var config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();
        @Cleanup final var executorService = Executors.newSingleThreadExecutor();
        final var registry = TimeLimiterRegistry.ofDefaults();
        registry.addConfiguration("cisnux_time_limiter_config", config);
        final var future = executorService.submit(this::slow);
        // success
//        System.out.println(future.get());
        final var timeLimiter = registry.timeLimiter("cisnux", "cisnux_time_limiter_config");
        final var callable = timeLimiter.decorateFutureSupplier(() -> future);
//        callable.call();
        // success if the time under 1 second (1000 milliseconds)
//        System.out.println(callable.call());
        Assertions.assertDoesNotThrow(callable::call);
        System.out.println(callable.call());
    }
}
