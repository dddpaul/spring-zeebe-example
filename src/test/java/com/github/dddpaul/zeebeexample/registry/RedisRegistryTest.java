package com.github.dddpaul.zeebeexample.registry;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "app.registry.redis.enabled=true"
})
public class RedisRegistryTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--notify-keyspace-events", "Ex");

    @Configuration
    @Import(RedisRegistryImpl.class)
    static class TestConfig {
        @Bean
        public RedissonClient redissonClient() {
            org.redisson.config.Config config = new org.redisson.config.Config();
            config.useSingleServer()
                    .setAddress("redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));
            return Redisson.create(config);
        }
    }

    @Autowired
    private RedisRegistryImpl registry;

    @Test
    void checkTimeoutsShouldRemoveExpiredKeysAndRunCallback() throws Exception {
        // given
        CountDownLatch latch = new CountDownLatch(1);
        registry.checkTimeouts(Duration.ofMillis(100), latch::countDown);
        long key = 123L;
        registry.put(key, Instant.now());

        // when
        boolean triggered = latch.await(1000, TimeUnit.MILLISECONDS);

        // then
        assertNull(registry.get(key), "Expired process was not removed");
        assertTrue(triggered, "Timeout callback was not triggered");
    }

    @AfterAll
    static void stopContainer() {
        redis.stop();
    }
}
