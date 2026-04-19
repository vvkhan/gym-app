package com.epam.gym.report.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trainer_workload")
@Getter
@Setter
@NoArgsConstructor
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    private String firstName;
    private String lastName;

    @Column(name = "is_active")
    private boolean isActive;

    /**
     * cascade = ALL + orphanRemoval: adding/removing years via this list
     * is automatically persisted and cleaned up
     */
    @OneToMany(mappedBy = "workload", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingYear> years = new ArrayList<>();
}
