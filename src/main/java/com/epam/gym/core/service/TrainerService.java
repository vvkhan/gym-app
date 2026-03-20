package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.TrainingSpecification;
import com.epam.gym.core.repository.TrainingTypeRepository;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@LogExecution
@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

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

    public Trainer createTrainer(String firstName, String lastName, UUID trainingTypeId) {
        TrainingType trainingType = trainingTypeRepository.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        return trainerRepository.save(buildTrainerForCreate(firstName, lastName, trainingType));
    }

    @Transactional
    public Trainer updateTrainer(String username, String firstName, String lastName,
                                 UUID trainingTypeId, Boolean isActive) {
        TrainingType trainingType = trainingTypeRepository.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        Trainer existing = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        User user = existing.getUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        existing.setSpecialization(trainingType);
        user.setIsActive(isActive);
        return trainerRepository.save(existing);
    }

    public Optional<Trainer> getTrainerById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return trainerRepository.findById(id);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return trainerRepository.findByUserUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }


    @Transactional
    public void activate(String username) {
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        if (Boolean.TRUE.equals(trainer.getUser().getIsActive())) {
            throw new IllegalStateException("Trainer is already active: " + username);
        }
        trainer.getUser().setIsActive(true);
        trainerRepository.save(trainer);
    }

    @Transactional
    public void deactivate(String username) {
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        if (Boolean.FALSE.equals(trainer.getUser().getIsActive())) {
            throw new IllegalStateException("Trainer is already inactive: " + username);
        }
        trainer.getUser().setIsActive(false);
        trainerRepository.save(trainer);
    }

    public List<Training> getTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                       String traineeUsername) {
        return trainingRepository.findAll(
                TrainingSpecification.byTrainerCriteria(username, fromDate, toDate, traineeUsername));
    }

    // Helper

    private Trainer buildTrainerForCreate(String firstName, String lastName, TrainingType trainingType) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(usernameGenerator.generateUsername(firstName, lastName))
                .password(passwordGenerator.generatePassword())
                .isActive(true)
                .build();
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);
        return trainer;
    }
}
