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
    private static final String SELF_APP_URL = "https://pf-monitor.herokuapp.com/health";

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
        // Todo - implement a concurrent queue get the URL to use
        String reqUrl = (Thread.currentThread().getId()%2 == 0) ? APP_URL : SELF_APP_URL;
        Health health = new Health();
        health.setStatus("UNKNOWN");
        try {
            health = getAppHealth(reqUrl);
        } catch (Exception e) {
            log.error("Impossible to get Health for: {}, reason: {}", reqUrl, e.getMessage());
        }
        log.info("Health for {}: {}", reqUrl, health.getStatus());
        if (!health.getStatus().equals("UP")) {
            Email faillureEmaill = Email.builder()
                .to("paulo.r.r.fernandes@gmail.com")
                .from("pfernand.monitor@gmail.com")
                .subject(String.format("PF-Monitor: %s is DOWN", reqUrl))
                .body(String.format("PF-Monitor: %s is DOWN", reqUrl))
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
