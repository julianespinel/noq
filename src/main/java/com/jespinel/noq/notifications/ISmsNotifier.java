package com.jespinel.noq.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ISmsNotifier {

    Logger logger = LoggerFactory.getLogger(ISmsNotifier.class);

    void send(String phoneNumber, String message);
}
