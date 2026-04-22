package com.epam.gym.report.messaging;

import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueListenerTest {

    @InjectMocks DeadLetterQueueListener listener;

    @Test
    void onDeadLetter_logsMessageWithoutThrowing() throws Exception {
        TextMessage message = mock(TextMessage.class);
        when(message.getJMSMessageID()).thenReturn("ID:test-1");
        when(message.getText()).thenReturn("{\"trainerUsername\":null}");

        assertThatCode(() -> listener.onDeadLetter(message))
                .doesNotThrowAnyException();
    }
}
