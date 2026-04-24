package com.epam.gym.core.messaging;

import com.epam.gym.core.dto.request.ActionType;
import com.epam.gym.core.dto.request.WorkloadRequest;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingEventPublisherTest {

    @Mock JmsTemplate jmsTemplate;

    @InjectMocks TrainingEventPublisher publisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "queue", "training.events");
    }

    @Test
    void publish_sendsToCorrectQueueWithTransactionIdProperty() throws Exception {
        WorkloadRequest request = new WorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2024, 3, 15), 60, ActionType.ADD);

        publisher.publish("tx-123", request);

        ArgumentCaptor<MessagePostProcessor> processorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq("training.events"), eq(request), processorCaptor.capture());

        Message message = mock(Message.class);
        processorCaptor.getValue().postProcessMessage(message);
        verify(message).setStringProperty("transactionId", "tx-123");
    }
}
