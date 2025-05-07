package com.github.dddpaul.zeebeexample.registry;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
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
@ContextConfiguration(initializers = RedisRegistryTest.Initializer.class)
public class RedisRegistryTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--notify-keyspace-events", "Ex");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext ctx) {
            String redisUrl = "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379);
            TestPropertyValues.of(
                    "spring.redis.url=" + redisUrl,
                    "spring.redis.host=" + redis.getHost(),
                    "spring.redis.port=" + redis.getMappedPort(6379)
            ).applyTo(ctx.getEnvironment());
        }
    }

    @Autowired
    private RedisRegistryImpl registry;

    @Test
    void checkTimeoutsShouldRemoveExpiredKeysAndRunCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        registry.checkTimeouts(Duration.ofMillis(100), latch::countDown);
        long key = 777L;
        registry.put(key, Instant.now());

        boolean triggered = latch.await(2, TimeUnit.SECONDS);

        assertTrue(triggered, "Timeout callback was not triggered");
        assertFalse(registry.all().containsKey(key), "Expired process was not removed");
    }

    @AfterAll
    static void stopContainer() {
        redis.stop();
    }

    @Configuration
    static class TestConfig {
        @Bean
        public RedissonClient redissonClient() {
            org.redisson.config.Config config = new org.redisson.config.Config();
            config.useSingleServer()
                    .setAddress("redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));
            return Redisson.create(config);
        }
    }
}
