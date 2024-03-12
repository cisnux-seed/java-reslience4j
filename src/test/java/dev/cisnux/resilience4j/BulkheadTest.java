package dev.cisnux.resilience4j;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import io.github.resilience4j.bulkhead.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class BulkheadTest {
    private final AtomicLong counter = new AtomicLong(0L);

    @SneakyThrows
    void slow() {
        final var value = counter.incrementAndGet();
        log.info("Slow\t: {}", value);
        Thread.sleep(1000L);
        log.info("complete {}", value);
    }

    @Test
    void testSemaphore() throws InterruptedException {
        final var bulkhead = Bulkhead.ofDefaults("cisnux");

        for (int i = 0; i < 1000; i++) {
            final var runnable = Bulkhead.decorateRunnable(bulkhead, this::slow);
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }


    @Test
    @SneakyThrows
    void testThreadPool() {
        log.info(String.valueOf((Runtime.getRuntime().availableProcessors())));
        @Cleanup final var bulkhead = ThreadPoolBulkhead.ofDefaults("cisnux");
        for (int i = 0; i < 1000; i++) {
            final var supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, this::slow);
            supplier.get();
        }
    }

    @Test
    void testSemaphoreConfig() throws InterruptedException {
        final var config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)
                .maxWaitDuration(Duration.ofSeconds(5))
                .build();
        final var bulkhead = Bulkhead.of("cisnux", config);

        // cuz runnable at 25 has been waited in 5 seconds (in 5 cycles) then it would be failed
        for (int i = 0; i < 100; i++) {
            final var runnable = Bulkhead.decorateRunnable(bulkhead, this::slow);
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @SneakyThrows
    @Test
    void testThreadPoolConfig() {
        final var config = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(5)
                .coreThreadPoolSize(5)
                // minimum capacity that needed for 20 runnables
                // numberOfRunnable - maxThreadPoolSize = minimumCapacity = 20 - 5 = 15
//                .queueCapacity(15)
                .build();
        log.info(String.valueOf((Runtime.getRuntime().availableProcessors())));
        @Cleanup final var bulkhead = ThreadPoolBulkhead.of("cisnux", config);

        for (int i = 0; i < 20; i++) {
            final var supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, this::slow);
            supplier.get();
        }
    }

    @Test
    void testSemaphoreRegistry() throws InterruptedException {
        final var config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)
                .maxWaitDuration(Duration.ofSeconds(5))
                .build();
        final var registry = BulkheadRegistry.ofDefaults();
        registry.addConfiguration("cisnux_bulkhead_semaphore", config);
        final var bulkhead = registry.bulkhead("cisnux", "cisnux_bulkhead_semaphore");

        // cuz runnable at 25 has been waited in 5 seconds (in 5 cycles) then it would be failed
        for (int i = 0; i < 100; i++) {
            final var runnable = Bulkhead.decorateRunnable(bulkhead, this::slow);
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @SneakyThrows
    @Test
    void testThreadPoolRegistry() {
        final var config = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(5)
                .coreThreadPoolSize(5)
                // minimum capacity that needed for 20 runnables
                // numberOfRunnable - maxThreadPoolSize = minimumCapacity = 20 - 5 = 15
                .queueCapacity(15)
                .build();
        log.info(String.valueOf((Runtime.getRuntime().availableProcessors())));
        @Cleanup final var registry = ThreadPoolBulkheadRegistry.ofDefaults();
        registry.addConfiguration("cisnux_thread_pool_bulkhead", config);
        final var bulkhead = registry.bulkhead("cisnux", "cisnux_thread_pool_bulkhead");

        for (int i = 0; i < 20; i++) {
            final var supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, this::slow);
            supplier.get();
        }
    }
}
