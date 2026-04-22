package com.epam.gym.report.messaging;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.service.ReportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingEventListenerTest {

    @Mock ReportService reportService;
    @Mock JmsTemplate jmsTemplate;
    @Mock Validator validator;

    @InjectMocks TrainingEventListener listener;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "deadLetterQueue", "training.events.dlq");
    }

    @Test
    void validMessage_isProcessedAndNotRoutedToDlq() {
        WorkloadRequest request = validRequest();
        when(validator.validate(request)).thenReturn(Set.of());

        listener.onTrainingEvent(request, "tx-123");

        verify(reportService).processWorkload(request);
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void invalidMessage_isRoutedToDlqAndNotProcessed() {
        WorkloadRequest request = new WorkloadRequest();
        ConstraintViolation<WorkloadRequest> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(Path.class));
        when(violation.getMessage()).thenReturn("must not be blank");
        when(validator.validate(request)).thenReturn(Set.of(violation));

        listener.onTrainingEvent(request, "tx-456");

        verify(jmsTemplate).convertAndSend("training.events.dlq", request);
        verifyNoInteractions(reportService);
    }

    private WorkloadRequest validRequest() {
        WorkloadRequest r = new WorkloadRequest();
        r.setTrainerUsername("john.doe");
        r.setFirstName("John");
        r.setLastName("Doe");
        r.setActive(true);
        r.setTrainingDate(LocalDate.of(2024, 3, 15));
        r.setTrainingDurationMinutes(60);
        r.setActionType(ActionType.ADD);
        return r;
    }
}
