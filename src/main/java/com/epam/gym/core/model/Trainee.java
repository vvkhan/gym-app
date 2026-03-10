package com.epam.gym.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@NamedEntityGraph(
    name = Trainee.FULL_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode(value = "trainers", subgraph = "trainers")
    },
    subgraphs = {
        @NamedSubgraph(name = "trainers", attributeNodes = {
            @NamedAttributeNode("user"),
            @NamedAttributeNode("specialization")
        })
    }
)
@Entity
@Table(name = "trainee")
public class Trainee {

    public static final String FULL_GRAPH = "Trainee.full";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @Column(name = "date_of_birth")
    @ToString.Include
    private LocalDate dateOfBirth;

    @Column
    @ToString.Include
    private String address;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings;

    @ManyToMany
    @JoinTable(name = "trainee2trainer", joinColumns = @JoinColumn(name = "trainee_id"),
        inverseJoinColumns = @JoinColumn(name = "trainer_id"))
    private Set<Trainer> trainers;
}
