package com.priyansu.workflow.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowContextConcurrencyTest {

    @Test
    @DisplayName("Should preserve concurrent updates from parallel workflow branches")
    void shouldPreserveConcurrentUpdates() throws Exception {
        WorkflowContext context = new WorkflowContext(Map.of());
        int branchCount = 8;
        int updatesPerBranch = 250;
        ExecutorService executor = Executors.newFixedThreadPool(branchCount);
        CountDownLatch ready = new CountDownLatch(branchCount);
        CountDownLatch start = new CountDownLatch(1);

        for (int branch = 0; branch < branchCount; branch++) {
            int branchId = branch;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int update = 0; update < updatesPerBranch; update++) {
                        context.put("branch-" + branchId + "-" + update, update);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(context.getData()).hasSize(branchCount * updatesPerBranch)
                .containsEntry("branch-0-0", 0)
                .containsEntry("branch-7-249", 249);
    }
}
