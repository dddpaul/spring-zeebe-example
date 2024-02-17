package com.github.dddpaul.zeebeexample.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.starter")
public record ProcessStarterConfiguration(
        long count,
        int threads,
        String process,
        Map<String, String> messages,
        boolean random,
        Map<String, String> variables
) {}
