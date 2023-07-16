package com.github.dddpaul.zeebeexample.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "app.starter")
public class ProcessStarterConfiguration {

    private long count = 0;

    private int threads = 1;

    private String process;

    private  boolean random;

    private Map<String, String> variables;
}
