package com.epam.gym.core.storage;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import lombok.Data;

import java.util.List;

@Data
public class InitialData {

    private List<Trainee> trainees;
    private List<Trainer> trainers;
    private List<Training> trainings;
    private List<TrainingType> trainingTypes;
}
