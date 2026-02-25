package com.epam.gym.core.service;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.dao.TrainerDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
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
import java.util.stream.Collectors;

@LogExecution
@Service
public class TraineeService {

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

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
    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeDao.create(buildTraineeForCreate(firstName, lastName, dateOfBirth, address));
    }

    @Transactional
    public Trainee updateTrainee(String username, String firstName, String lastName,
                                 LocalDate dateOfBirth, String address, Boolean isActive) {
        Trainee existing = traineeDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        existing.getUser().setFirstName(firstName);
        existing.getUser().setLastName(lastName);
        existing.setDateOfBirth(dateOfBirth);
        existing.setAddress(address);
        existing.getUser().setIsActive(isActive);
        return traineeDao.update(existing);
    }

    @Transactional
    public void deleteTrainee(String username) {
        traineeDao.delete(username);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return traineeDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return traineeDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        return traineeDao.findAll();
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        return traineeDao.findByUsername(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        if (!trainee.getUser().getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        trainee.getUser().setPassword(newPassword);
        traineeDao.update(trainee);
    }

    @Transactional
    public void activate(String username) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        if (Boolean.TRUE.equals(trainee.getUser().getIsActive())) {
            throw new IllegalStateException("Trainee is already active: " + username);
        }
        trainee.getUser().setIsActive(true);
        traineeDao.update(trainee);
    }

    @Transactional
    public void deactivate(String username) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        if (Boolean.FALSE.equals(trainee.getUser().getIsActive())) {
            throw new IllegalStateException("Trainee is already inactive: " + username);
        }
        trainee.getUser().setIsActive(false);
        traineeDao.update(trainee);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                       String trainerName, String trainingTypeName) {
        return traineeDao.findTrainingsByCriteria(username, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedTrainers(String traineeUsername) {
        return trainerDao.findNotAssignedTrainers(traineeUsername);
    }

    @Transactional
    public Trainee updateTrainers(String traineeUsername, List<String> trainerUsernames) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + traineeUsername));
        List<Trainer> trainers = trainerUsernames.stream()
                .map(u -> trainerDao.findByUsername(u)
                        .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + u)))
                .collect(Collectors.toList());
        trainee.setTrainers(trainers);
        return traineeDao.update(trainee);
    }

    // Helper

    private Trainee buildTraineeForCreate(String firstName, String lastName,
                                          LocalDate dateOfBirth, String address) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(usernameGenerator.generateUsername(firstName, lastName))
                .password(passwordGenerator.generatePassword())
                .isActive(true)
                .build();
        return Trainee.builder()
                .user(user)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();
    }
}
