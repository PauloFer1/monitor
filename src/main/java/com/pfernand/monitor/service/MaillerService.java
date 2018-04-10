package com.pfernand.monitor.service;

import com.pfernand.monitor.exceptions.MaillerException;

public interface MaillerService {

    void sendSimpleMessage(String to, String from, String subject, String body) throws MaillerException;
}
