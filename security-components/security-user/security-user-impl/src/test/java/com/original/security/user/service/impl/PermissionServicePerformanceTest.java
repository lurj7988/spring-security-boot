package com.original.security.user.service.impl;

import com.original.security.config.SecurityProperties;
import com.original.security.user.entity.Permission;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServicePerformanceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityProperties securityProperties;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        when(securityProperties.getCache()).thenReturn(new SecurityProperties.Cache());
        permissionService = new PermissionServiceImpl(userRepository, securityProperties);
    }

    @Test
    void testHasPermission_ConcurrentPerformance_ShouldBeUnder5ms() throws InterruptedException {
        User user = new User("admin", "pwd", "admin@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        Permission perm = new Permission("user:read", "Read User");
        role.addPermission(perm);
        user.addRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // Pre-warm cache and JIT/Mockito
        for (int i = 0; i < 10000; i++) {
            permissionService.hasPermission("admin", "user:read");
        }

        int threadCount = 10;
        int iterationsPerThread = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        AtomicLong maxLatencyNano = new AtomicLong(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterationsPerThread; j++) {
                        long start = System.nanoTime();
                        boolean hasPerm = permissionService.hasPermission("admin", "user:read");
                        long duration = System.nanoTime() - start;
                        
                        // Ignore the very first few calls to avoid scheduler/thread-start anomalies
                        if (j > 10) {
                            long currentMax = maxLatencyNano.get();
                            while (duration > currentMax) {
                                if (maxLatencyNano.compareAndSet(currentMax, duration)) {
                                    break;
                                }
                                currentMax = maxLatencyNano.get();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        assertTrue(endLatch.await(10, TimeUnit.SECONDS), "Performance test timed out");
        
        executorService.shutdown();

        // 5ms = 5,000,000 ns
        long maxLatencyMs = maxLatencyNano.get() / 1_000_000;
        System.out.println("Max latency for concurrent hasPermission checks: " + maxLatencyMs + " ms");
        assertTrue(maxLatencyMs < 5, "Max latency should be under 5ms, but was " + maxLatencyMs + "ms");
    }
}
