package com.jespinel.noq.notifications;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        value = "smsNotifier",
        havingValue = "twilio",
        matchIfMissing = true
)
public class TwilioSmsNotifier implements ISmsNotifier {

    @Value("${twilio.account.sid}")
    private String sid;

    @Value("${twilio.account.token}")
    private String token;

    @Value("${twilio.sender.number}")
    private String senderNumber;

    private final PhoneNumber sender;

    public TwilioSmsNotifier() {
        Twilio.init(sid, token);
        sender = new PhoneNumber(senderNumber);
    }

    @Override
    public void send(String phoneNumber, String message) {
        PhoneNumber to = new PhoneNumber(phoneNumber);
        Message sentMessage = Message.creator(to, sender, message).create();
        logger.debug("TWILIO: Message sent to %s, message sid %s".
                formatted(phoneNumber, sentMessage.getSid()));
    }
}
