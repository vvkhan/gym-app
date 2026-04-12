package com.epam.gym.report.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "training_year")
@Getter
@Setter
@NoArgsConstructor
public class TrainingYear {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workload_id", nullable = false)
    private TrainerWorkload workload;

    @OneToMany(mappedBy = "trainingYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingMonth> months = new ArrayList<>();
}
