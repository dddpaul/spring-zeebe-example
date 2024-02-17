package com.github.dddpaul.zeebeexample.actuator;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component
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

    public void reset() {
        created.set(0);
        approved.set(0);
        rejected.set(0);
    }

    public AtomicLong getCreated() {
        return created;
    }

    public AtomicLong getApproved() {
        return approved;
    }

    public AtomicLong getRejected() {
        return rejected;
    }
}
