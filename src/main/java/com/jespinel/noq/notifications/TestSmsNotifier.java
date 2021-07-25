package com.jespinel.noq.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "smsNotifier", havingValue = "test")
public class TestSmsNotifier implements ISmsNotifier {

    @Override
    public void send(String phoneNumber, String message) {
        logger.debug("TEST: Message sent to %s".formatted(phoneNumber));
    }
}
