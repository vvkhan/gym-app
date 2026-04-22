package com.epam.gym.report.messaging;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterQueueListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueListener.class);

    /**
     * Receives invalid messages that failed validation in {@link TrainingEventListener}.
     * Logs the raw payload for inspection and alerting.
     */
    @JmsListener(destination = "${app.messaging.dead-letter-queue}")
    public void onDeadLetter(Message message) {
        try {
            String body = message instanceof TextMessage tm ? tm.getText() : message.toString();
            log.error("Dead letter received — jmsId={}, body={}", message.getJMSMessageID(), body);
        } catch (Exception e) {
            log.error("Failed to read dead letter message", e);
        }
    }
}
