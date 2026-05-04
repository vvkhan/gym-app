package com.epam.gym.report.health;

import com.epam.gym.report.repository.TrainerSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadStorageHealthIndicatorTest {

    @Mock
    TrainerSummaryRepository repository;

    @InjectMocks
    WorkloadStorageHealthIndicator indicator;

    @Test
    void health_returnsUpWithTrackedTrainerCount() {
        when(repository.count()).thenReturn(5L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("trackedTrainers", 5L);
    }

    @Test
    void health_returnsDownWhenStorageUnavailable() {
        when(repository.count()).thenThrow(new RuntimeException("MongoDB unavailable"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("reason").toString()).contains("MongoDB unavailable");
    }
}
