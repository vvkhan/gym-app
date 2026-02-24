package com.epam.gym.core.service;

import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.dao.TrainingTypeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@LogExecution
@Service
public class TrainerService {

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer createTrainer(String firstName, String lastName, Long trainingTypeId) {
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        return trainerDao.create(buildTrainerForCreate(firstName, lastName, trainingType));
    }

    public Trainer updateTrainer(Long id, String firstName, String lastName, Long trainingTypeId, Boolean isActive) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        Trainer existing = trainerDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + id + " not found"));
        existing.setFirstName(firstName);
        existing.setLastName(lastName);
        existing.setSpecialization(trainingType);
        existing.setIsActive(isActive);
        return trainerDao.update(existing);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return trainerDao.findById(id);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return trainerDao.findByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        return trainerDao.findAll();
    }

    // Helper

    private Trainer buildTrainerForCreate(String firstName, String lastName, TrainingType trainingType) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        return Trainer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(password)
                .isActive(true)
                .specialization(trainingType)
                .build();
    }
}
