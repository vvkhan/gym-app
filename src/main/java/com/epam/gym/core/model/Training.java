package com.epam.gym.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@NamedEntityGraph(
    name = Training.FULL_GRAPH,
    attributeNodes = {
        @NamedAttributeNode(value = "trainee", subgraph = "trainee"),
        @NamedAttributeNode(value = "trainer", subgraph = "trainer"),
        @NamedAttributeNode("trainingType")
    },
    subgraphs = {
        @NamedSubgraph(name = "trainee", attributeNodes = @NamedAttributeNode("user")),
        @NamedSubgraph(name = "trainer", attributeNodes = {
            @NamedAttributeNode("user"),
            @NamedAttributeNode("specialization")
        })
    }
)
@Entity
@Table(name = "training")

public class Training {

    public static final String FULL_GRAPH = "Training.full";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "training_name", nullable = false)
    @ToString.Include
    private String trainingName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @Column(name = "training_date", nullable = false)
    @ToString.Include
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    @ToString.Include
    private Integer duration;
}
