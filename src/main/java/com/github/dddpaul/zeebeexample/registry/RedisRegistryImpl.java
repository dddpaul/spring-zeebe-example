package com.github.dddpaul.zeebeexample.registry;

import jakarta.annotation.PostConstruct;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@ConditionalOnProperty(value = "app.registry.redis.enabled", havingValue = "true")
public class RedisRegistryImpl implements ProcessRegistry {

    @Autowired
    private RedissonClient redissonClient;

    private RMap<Long, Instant> processes;

    @PostConstruct
    public void init() {
        this.processes = redissonClient.getMap("zeebe:process:times");
    }

    @Override
    public Map<Long, Instant> all() {
        return processes.readAllMap();
    }

    @Override
    public Instant get(long key) {
        return processes.get(key);
    }

    @Override
    public void put(long key, Instant timestamp) {
        processes.put(key, timestamp);
    }

    @Override
    public void remove(Long key) {
        processes.remove(key);
    }
}
