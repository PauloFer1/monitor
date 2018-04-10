package com.pfernand.monitor.service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
public class HealthScheduler {

    private final HealthChecker healthChecker;

    private final ScheduledExecutorService scheduledExecutorService;

    public HealthScheduler(HealthChecker healthChecker, ScheduledExecutorService scheduledExecutorService) {
        this.healthChecker = healthChecker;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @PostConstruct
    public void scheduleHealthChecker() {
        log.info("Initializing Health Checker Scheduler");
        scheduledExecutorService.scheduleAtFixedRate(healthChecker, 10, 10, TimeUnit.SECONDS);
    }

}
