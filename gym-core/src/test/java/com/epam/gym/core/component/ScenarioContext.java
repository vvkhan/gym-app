package com.epam.gym.core.component;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import io.cucumber.java.Before;
import io.cucumber.spring.ScenarioScope;

import java.util.List;
import java.util.UUID;

@ScenarioScope
public class ScenarioContext {
    public String uid;
    public Trainee trainee;
    public Trainee trainee2;
    public Trainer trainer;
    public String deletedTraineeUsername;
    public boolean optionalWasEmpty;
    public Exception thrownException;
    public List<Trainer> notAssignedTrainers;
    public List<Training> filteredTrainings;
    public List<TrainingType> allTrainingTypes;
    public Training createdTraining;
    public String newPassword;

    @Before
    public void init() {
        uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        trainee = null;
        trainee2 = null;
        trainer = null;
        deletedTraineeUsername = null;
        optionalWasEmpty = false;
        thrownException = null;
        notAssignedTrainers = null;
        filteredTrainings = null;
        allTrainingTypes = null;
        createdTraining = null;
        newPassword = null;
    }
}
