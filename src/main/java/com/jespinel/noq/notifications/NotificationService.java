package com.jespinel.noq.notifications;

import com.jespinel.noq.turns.Turn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final String TURN_REQUESTED_TEMPLATE =
            "Your turn has been requested, you got turn %s";

    private final ISmsNotifier smsNotifier;

    @Autowired
    public NotificationService(ISmsNotifier smsNotifier) {
        this.smsNotifier = smsNotifier;
    }

    public void notifyTurnCreation(Turn turn) {
        String message = TURN_REQUESTED_TEMPLATE.formatted(turn.getTurnNumber());
        smsNotifier.send(message, turn.getPhoneNumber());
    }
}
