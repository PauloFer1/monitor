package com.pfernand.monitor.service;

import com.pfernand.monitor.exceptions.MaillerException;
import com.pfernand.monitor.model.Email;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@Named
public class MaillerServiceSpringImpl implements MaillerService {

    private final JavaMailSender javaMailSender;


    @Inject
    public MaillerServiceSpringImpl(final JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendSimpleMessage(String to, String from, String subject, String body) throws MaillerException {

        log.info("Sending email to: {}", to);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setReplyTo(from);

        try {
            javaMailSender.send(simpleMailMessage);
        } catch (Exception e) {
            log.error("Email failed to send with params: to[{}], from[{}], subject[{}], body[{}]",
                    to, from, subject, body);
            throw new MaillerException("Email failed to send", e);
        }
    }

    public void sendSimpleMessage(Email email) throws MaillerException {
        this.sendSimpleMessage(email.getTo(), email.getFrom(), email.getSubject(), email.getBody());
    }
}
