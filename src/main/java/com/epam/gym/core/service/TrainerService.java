package com.epam.gym.core.service;

import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.dao.TrainingTypeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.model.User;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Transactional
    public Trainer createTrainer(String firstName, String lastName, Long trainingTypeId) {
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        return trainerDao.create(buildTrainerForCreate(firstName, lastName, trainingType));
    }

    @Transactional
    public Trainer updateTrainer(String username, String firstName, String lastName,
                                 Long trainingTypeId, Boolean isActive) {
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        Trainer existing = trainerDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        existing.getUser().setFirstName(firstName);
        existing.getUser().setLastName(lastName);
        existing.setSpecialization(trainingType);
        existing.getUser().setIsActive(isActive);
        return trainerDao.update(existing);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return trainerDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return trainerDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        return trainerDao.findAll();
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        return trainerDao.findByUsername(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        if (!trainer.getUser().getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        trainer.getUser().setPassword(newPassword);
        trainerDao.update(trainer);
    }

    @Transactional
    public void activate(String username) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        if (Boolean.TRUE.equals(trainer.getUser().getIsActive())) {
            throw new IllegalStateException("Trainer is already active: " + username);
        }
        trainer.getUser().setIsActive(true);
        trainerDao.update(trainer);
    }

    @Transactional
    public void deactivate(String username) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        if (Boolean.FALSE.equals(trainer.getUser().getIsActive())) {
            throw new IllegalStateException("Trainer is already inactive: " + username);
        }
        trainer.getUser().setIsActive(false);
        trainerDao.update(trainer);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                       String traineeName) {
        return trainerDao.findTrainingsByCriteria(username, fromDate, toDate, traineeName);
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
        return Trainer.builder()
                .user(user)
                .specialization(trainingType)
                .build();
    }
}
