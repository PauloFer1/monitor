package com.pfernand.monitor.service;

import com.pfernand.monitor.exceptions.MaillerException;
import com.pfernand.monitor.model.Email;
import com.pfernand.monitor.model.Health;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Named
public class HealthChecker implements Runnable {

    // Todo, set this value in properties
    private static final String APP_URL = "https://pf-mailler.herokuapp.com/health";

    private final RestTemplate restTemplate;

    private final MaillerServiceSpringImpl maillerServiceSpring;


    @Inject
    public HealthChecker(RestTemplate restTemplate, MaillerServiceSpringImpl maillerServiceSpring) {
        this.restTemplate = restTemplate;
        this.maillerServiceSpring = maillerServiceSpring;
    }

    private Health getAppHealth(String appUrl) throws RestClientException {
        log.info("Getting health for {}", appUrl);
        return restTemplate.getForObject(appUrl, Health.class);
    }

    @Override
    public void run() {
        Health health = new Health();
        health.setStatus("UNKNOWN");
        try {
            health = getAppHealth(APP_URL);
        } catch (Exception e) {
            log.error("Impossible to get Health for: {}, reason: {}", APP_URL, e.getMessage());
        }
        log.info("Health for {}: {}", APP_URL, health.getStatus());
        if (!health.getStatus().equals("UP")) {
            Email faillureEmaill = Email.builder()
                .to("paulo.r.r.fernandes@gmail.com")
                .from("pfernand.monitor@gmail.com")
                .subject(String.format("PF-Monitor: %s is DOWN", APP_URL))
                .body(String.format("PF-Monitor: %s is DOWN", APP_URL))
                .build();
            try {
                maillerServiceSpring.sendSimpleMessage(faillureEmaill);
            } catch (MaillerException e) {
                log.error("Failed to send email: {}, reason: {}", faillureEmaill.toString(), e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
