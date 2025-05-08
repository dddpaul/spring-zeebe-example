package com.github.dddpaul.zeebeexample.registry;

import java.time.Duration;
import java.time.Instant;

public interface ProcessRegistry {
    Instant get(long key);

    void put(long key, Instant timestamp);

    void remove(long key);

    void setExpiration(Duration timeout, Runnable callback);
}
