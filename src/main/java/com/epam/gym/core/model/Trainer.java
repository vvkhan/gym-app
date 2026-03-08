package com.epam.gym.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@NamedEntityGraph(
    name = Trainer.FULL_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("specialization")
    }
)
@Entity
@Table(name = "trainer")
public class Trainer {

    public static final String FULL_GRAPH = "Trainer.full";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "trainers")
    private List<Trainee> trainees;
}
