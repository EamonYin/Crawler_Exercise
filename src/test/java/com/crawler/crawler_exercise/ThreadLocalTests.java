package com.crawler.crawler_exercise;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class ThreadLocalTests {

    private static final ThreadLocal<String> LOG_CONTEXT = new ThreadLocal<>();

    @Test
    void threadLocal_logExample_completableFuture() {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            LOG_CONTEXT.set("REQ-A");
            log.info("cf-1 log, requestId={}" , LOG_CONTEXT.get());
            String value = LOG_CONTEXT.get();
            LOG_CONTEXT.remove();
            return value;
        });

        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            LOG_CONTEXT.set("REQ-B");
            log.info("cf-2 log, requestId={}" , LOG_CONTEXT.get());
            String value = LOG_CONTEXT.get();
            LOG_CONTEXT.remove();
            return value;
        });

        CompletableFuture.allOf(f1, f2).join();

        String r1 = f1.join();
        String r2 = f2.join();
        System.out.println("completableFuture result: r1=" + r1 + ", r2=" + r2);
        Assertions.assertEquals("REQ-A", r1, "r1 expected REQ-A, actual=" + r1);
        Assertions.assertEquals("REQ-B", r2, "r2 expected REQ-B, actual=" + r2);
        Assertions.assertNotEquals(r1, r2, "r1 and r2 should be different, actual r1=" + r1 + ", r2=" + r2);
    }

    @Test
    void threadLocal_logExample_completableFuture_withExecutor() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
                LOG_CONTEXT.set("REQ-A");
                System.out.println("cf-executor-1 log, requestId=" + LOG_CONTEXT.get());
                String value = LOG_CONTEXT.get();
                LOG_CONTEXT.remove();
                return value;
            }, executor);

            CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
                LOG_CONTEXT.set("REQ-B");
                System.out.println("cf-executor-2 log, requestId=" + LOG_CONTEXT.get());
                String value = LOG_CONTEXT.get();
                LOG_CONTEXT.remove();
                return value;
            }, executor);

            CompletableFuture.allOf(f1, f2).join();

            String r1 = f1.join();
            String r2 = f2.join();
            System.out.println("completableFuture with executor result: r1=" + r1 + ", r2=" + r2);
            Assertions.assertEquals("REQ-A", r1, "r1 expected REQ-A, actual=" + r1);
            Assertions.assertEquals("REQ-B", r2, "r2 expected REQ-B, actual=" + r2);
            Assertions.assertNotEquals(r1, r2, "r1 and r2 should be different, actual r1=" + r1 + ", r2=" + r2);
        } finally {
            executor.shutdown();
            executor.awaitTermination(3, TimeUnit.SECONDS);
        }
    }

}
