package com.epam.gym.report.service;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.mapper.WorkloadMapper;
import com.epam.gym.report.model.TrainerSummary;
import com.epam.gym.report.model.TrainerSummary.MonthSummary;
import com.epam.gym.report.model.TrainerSummary.YearSummary;
import com.epam.gym.report.repository.TrainerSummaryRepository;
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
    TrainerSummaryRepository repository;

    @Mock
    WorkloadMapper workloadMapper;

    @InjectMocks
    ReportService service;

    @Test
    void processWorkload_addCreatesDocumentAndAccumulatesDuration() {
        when(repository.findByUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request(ActionType.ADD, 60));

        ArgumentCaptor<TrainerSummary> captor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(repository).save(captor.capture());
        TrainerSummary saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("john.doe");
        assertThat(saved.getYears()).hasSize(1);
        assertThat(saved.getYears().get(0).getMonths().get(0).getTotalDurationMinutes()).isEqualTo(60);
    }

    @Test
    void processWorkload_addAccumulatesDurationOnExistingMonth() {
        TrainerSummary existing = existingSummary(2024, 3, 30);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request(ActionType.ADD, 45));

        int result = existing.getYears().get(0).getMonths().get(0).getTotalDurationMinutes();
        assertThat(result).isEqualTo(75);
    }

    @Test
    void processWorkload_addCreatesNewMonthWhenYearExists() {
        TrainerSummary existing = existingSummary(2024, 1, 60);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Request for March — different month than existing January entry
        WorkloadRequest req = request(ActionType.ADD, 30);
        req.setTrainingDate(LocalDate.of(2024, 3, 10));
        service.processWorkload(req);

        YearSummary year = existing.getYears().get(0);
        assertThat(year.getMonths()).hasSize(2);
    }

    @Test
    void processWorkload_deleteSubtractsDuration() {
        TrainerSummary existing = existingSummary(2024, 3, 90);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request(ActionType.DELETE, 40));

        int result = existing.getYears().get(0).getMonths().get(0).getTotalDurationMinutes();
        assertThat(result).isEqualTo(50);
    }

    @Test
    void processWorkload_deleteFloorsAtZeroOnOverflow() {
        // Existing has 30 min; deleting 60 must not produce a negative value
        TrainerSummary existing = existingSummary(2024, 3, 30);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request(ActionType.DELETE, 60));

        int result = existing.getYears().get(0).getMonths().get(0).getTotalDurationMinutes();
        assertThat(result).isEqualTo(0);
    }

    @Test
    void processWorkload_syncesProfileFieldsOnExistingDocument() {
        TrainerSummary existing = existingSummary(2024, 3, 0);
        existing.setFirstName("OldFirst");
        existing.setLastName("OldLast");
        existing.setActive(false);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request(ActionType.ADD, 30));

        assertThat(existing.getFirstName()).isEqualTo("John");
        assertThat(existing.getLastName()).isEqualTo("Doe");
        assertThat(existing.isActive()).isTrue();
    }

    @Test
    void getSummary_throwsWhenTrainerHasNoDocument() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSummary("unknown"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("unknown");
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

    private TrainerSummary existingSummary(int year, int month, int duration) {
        MonthSummary m = new MonthSummary(month);
        m.setTotalDurationMinutes(duration);

        YearSummary y = new YearSummary(year);
        y.getMonths().add(m);

        TrainerSummary s = new TrainerSummary();
        s.setUsername("john.doe");
        s.setFirstName("John");
        s.setLastName("Doe");
        s.setActive(true);
        s.getYears().add(y);
        return s;
    }
}
