package com.epam.gym.report.service;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.mapper.WorkloadMapper;
import com.epam.gym.report.model.TrainerWorkload;
import com.epam.gym.report.model.TrainingMonth;
import com.epam.gym.report.model.TrainingYear;
import com.epam.gym.report.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    TrainerWorkloadRepository repository;

    @Mock
    WorkloadMapper workloadMapper;

    @InjectMocks
    ReportService service;

    @Test
    void processWorkload_addCreatesWorkloadAndAccumulatesDuration() {
        when(repository.findByUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request(ActionType.ADD, 60));

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        TrainerWorkload saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("john.doe");
        assertThat(saved.getYears()).hasSize(1);
        assertThat(saved.getYears().get(0).getMonths().get(0).getTotalDurationMinutes()).isEqualTo(60);
    }

    @Test
    void processWorkload_deleteSubtractsDurationAndFloorsAtZero() {
        // Existing record has 30 minutes; deleting 60 must not go below 0
        TrainerWorkload existing = existingWorkload(2024, 3, 30);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request(ActionType.DELETE, 60));

        int remaining = existing.getYears().get(0).getMonths().get(0).getTotalDurationMinutes();
        assertThat(remaining).isEqualTo(0);
    }

    @Test
    void getSummary_throwsWhenTrainerHasNoWorkload() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSummary("unknown"))
                .isInstanceOf(NoSuchElementException.class);
    }

    // Helpers

    private WorkloadRequest request(ActionType action, int duration) {
        WorkloadRequest r = new WorkloadRequest();
        r.setTrainerUsername("john.doe");
        r.setFirstName("John");
        r.setLastName("Doe");
        r.setActive(true);
        r.setTrainingDate(LocalDate.of(2024, 3, 15));
        r.setTrainingDurationMinutes(duration);
        r.setActionType(action);
        return r;
    }

    private TrainerWorkload existingWorkload(int year, int month, int duration) {
        TrainingMonth m = new TrainingMonth();
        m.setMonth(month);
        m.setTotalDurationMinutes(duration);

        TrainingYear y = new TrainingYear();
        y.setYear(year);
        y.getMonths().add(m);
        m.setTrainingYear(y);

        TrainerWorkload w = new TrainerWorkload();
        w.setUsername("john.doe");
        w.getYears().add(y);
        y.setWorkload(w);
        return w;
    }
}
