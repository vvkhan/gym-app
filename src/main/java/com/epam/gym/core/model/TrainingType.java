package com.epam.gym.core.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Entity
@Table(name = "training_type")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", unique = true, nullable = false)
    private String trainingTypeName;
}
