package com.pfernand.monitor.service;

import com.pfernand.monitor.exceptions.MaillerException;
import com.pfernand.monitor.model.Email;
import com.pfernand.monitor.model.Health;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Named
public class HealthChecker implements Runnable {

    // Todo, set this value in properties
    private final String[] appUrls;

    private final RestTemplate restTemplate;

    private final MaillerServiceSpringImpl maillerServiceSpring;

    private AtomicInteger urlPosition = new AtomicInteger(0);


    @Inject
    public HealthChecker(
        RestTemplate restTemplate,
        MaillerServiceSpringImpl maillerServiceSpring,
        @Value("${app.urls}") String[] appUrls) {
        this.restTemplate = restTemplate;
        this.maillerServiceSpring = maillerServiceSpring;
        this.appUrls = appUrls;
    }

    private Health getAppHealth(String appUrl) throws RestClientException {
        log.info("Getting health for {}", appUrl);
        return restTemplate.getForObject(appUrl, Health.class);
    }

    @Override
    public void run() {
        Health health = new Health();
        health.setStatus("UNKNOWN");
        if (urlPosition.incrementAndGet() >= appUrls.length) {
            urlPosition.set(0);
        }
        try {
            health = getAppHealth(appUrls[urlPosition.get()]);
        } catch (Exception e) {
            log.error("Impossible to get Health for: {}, reason: {}", appUrls[urlPosition.get()], e.getMessage());
        }
        log.info("Health for {}: {}", appUrls[urlPosition.get()], health.getStatus());
        if (!health.getStatus().equals("UP")) {
            sendDownEmail();
        }
    }

    private void sendDownEmail() {
        Email faillureEmaill = Email.builder()
            .to("paulo.r.r.fernandes@gmail.com")
            .from("pfernand.monitor@gmail.com")
            .subject(String.format("PF-Monitor: %s is DOWN", appUrls[urlPosition.get()]))
            .body(String.format("PF-Monitor: %s is DOWN", appUrls[urlPosition.get()]))
            .build();
        try {
            maillerServiceSpring.sendSimpleMessage(faillureEmaill);
        } catch (MaillerException e) {
            log.error("Failed to send email: {}, reason: {}", faillureEmaill.toString(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
