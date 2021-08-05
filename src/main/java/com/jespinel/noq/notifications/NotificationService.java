package com.jespinel.noq.notifications;

import com.jespinel.noq.turns.Turn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final ISmsNotifier smsNotifier;

    @Autowired
    public NotificationService(ISmsNotifier smsNotifier) {
        this.smsNotifier = smsNotifier;
    }

    public void notifyTurnCreation(Turn turn) {
        String turnRequestedTemplate = "Your turn has been requested, you got turn %s";
        String message = turnRequestedTemplate.formatted(turn.getTurnNumber());
        smsNotifier.send(message, turn.getPhoneNumber());
    }

    public void notifyCancellation(Turn turn) {
        String turnRequestedTemplate = "The turn %s has been cancelled";
        String message = turnRequestedTemplate.formatted(turn.getTurnNumber());
        smsNotifier.send(message, turn.getPhoneNumber());
    }

    public void notifyError(String errorMessage, String phoneNumber) {
        smsNotifier.send(errorMessage, phoneNumber);
    }

    public void notifyReadiness(Turn turn) {
        String turnReadyTemplate = "The turn %s is ready";
        String message = turnReadyTemplate.formatted(turn.getTurnNumber());
        smsNotifier.send(message, turn.getPhoneNumber());
    }
}
