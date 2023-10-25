package com.github.dddpaul.zeebeexample.actuator;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Data
public class ApplicationStats {
    private AtomicLong created = new AtomicLong(0);
    private AtomicLong approved = new AtomicLong(0);
    private AtomicLong rejected = new AtomicLong(0);

    public void incrementCreated() {
        created.incrementAndGet();
    }

    public void incrementApproved() {
        approved.incrementAndGet();
    }

    public void incrementRejected() {
        rejected.incrementAndGet();
    }
}
