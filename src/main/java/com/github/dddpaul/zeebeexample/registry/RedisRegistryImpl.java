package com.github.dddpaul.zeebeexample.registry;

import jakarta.annotation.PostConstruct;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnProperty(value = "app.registry.redis.enabled", havingValue = "true")
public class RedisRegistryImpl implements ProcessRegistry {

    @Autowired
    private RedissonClient redissonClient;

    private RMap<Long, Instant> processes;
    private Duration timeout;

    @PostConstruct
    public void init() {
        this.processes = redissonClient.getMap("zeebe:process:times");
    }

    @Override
    public Instant get(long key) {
        return processes.get(key);
    }

    @Override
    public void remove(long key) {
        processes.remove(key);
    }

    @Override
    public void put(long key, Instant timestamp) {
        processes.put(key, timestamp);
        RBucket<String> bucket = redissonClient.getBucket("zeebe:timeout:" + key);
        bucket.set("", timeout);
    }

    @Override
    public void setExpiration(Duration timeout, Runnable callback) {
        this.timeout = timeout;
        RTopic topic = redissonClient.getTopic("__keyevent@0__:expired", StringCodec.INSTANCE);
        topic.addListener(String.class, (channel, expiredKey) -> {
            if (expiredKey.startsWith("zeebe:timeout:")) {
                long key = Long.parseLong(expiredKey.substring("zeebe:timeout:".length()));
                processes.remove(key);
                callback.run();
            }
        });
    }
}
