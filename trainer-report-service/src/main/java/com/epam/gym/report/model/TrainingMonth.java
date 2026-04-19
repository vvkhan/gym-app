package com.epam.gym.report.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "training_month")
@Getter
@Setter
@NoArgsConstructor
public class TrainingMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 1 = January ... 12 = December
    private int month;

    @Column(name = "total_duration_minutes")
    private int totalDurationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false)
    private TrainingYear trainingYear;
}
