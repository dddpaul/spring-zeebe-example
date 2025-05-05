package com.github.dddpaul.zeebeexample.registry;

import java.time.Instant;
import java.util.Map;

public interface ProcessRegistry {
    Map<Long, Instant> all();
    Instant get(long key);
    void put(long key, Instant timestamp);
    void remove(Long key);
}
