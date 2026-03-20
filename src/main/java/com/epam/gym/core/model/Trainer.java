package com.epam.gym.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@NamedEntityGraph(
    name = Trainer.FULL_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("specialization"),
        @NamedAttributeNode(value = "trainees", subgraph = "trainees-subgraph")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "trainees-subgraph",
            attributeNodes = @NamedAttributeNode("user")
        )
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
    private Set<Trainee> trainees;
}
