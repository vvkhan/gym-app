package com.epam.gym.report.messaging;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.service.ReportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingEventListenerTest {

    @Mock ReportService reportService;
    @Mock Validator validator;

    @InjectMocks TrainingEventListener listener;

    @Test
    void validMessage_isProcessed() {
        WorkloadRequest request = validRequest();
        when(validator.validate(request)).thenReturn(Set.of());

        listener.onTrainingEvent(request, "tx-123");

        verify(reportService).processWorkload(request);
    }

    @Test
    void invalidMessage_throwsSoBrokerRoutesToDlq() {
        WorkloadRequest request = new WorkloadRequest();
        ConstraintViolation<WorkloadRequest> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(Path.class));
        when(violation.getMessage()).thenReturn("must not be blank");
        when(validator.validate(request)).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> listener.onTrainingEvent(request, "tx-456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid WorkloadRequest");

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
