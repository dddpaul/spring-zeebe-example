package com.github.dddpaul.zeebeexample.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "stats")
public class StatsEndpoint {

    @Autowired
    private ApplicationStats stats;

    @ReadOperation
    public ApplicationStats getStats() {
        return stats;
    }

}
