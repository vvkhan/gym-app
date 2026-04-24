package com.epam.gym.core.messaging;

import com.epam.gym.core.dto.request.WorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrainingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TrainingEventPublisher.class);

    private final JmsTemplate jmsTemplate;

    @Value("${app.messaging.training-events-queue}")
    private String queue;

    public TrainingEventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Publishes a training workload event to the ActiveMQ queue.
     * The transactionId is attached as a JMS property for end-to-end log correlation.
     */
    public void publish(String txId, WorkloadRequest request) {
        jmsTemplate.convertAndSend(queue, request, message -> {
            message.setStringProperty("transactionId", txId);
            return message;
        });
        log.debug("[txId={}] Published {} event for trainer={}", txId, request.getActionType(),
                request.getTrainerUsername());
    }
}
