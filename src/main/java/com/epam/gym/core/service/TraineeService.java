package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.TraineeRepository;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.TrainerSpecification;
import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.TrainingSpecification;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@LogExecution
@Service
public class TraineeService {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

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

    @Transactional
    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeRepository.save(buildTraineeForCreate(firstName, lastName, dateOfBirth, address));
    }

    @Transactional
    public Trainee updateTrainee(String username, String firstName, String lastName,
                                 LocalDate dateOfBirth, String address, Boolean isActive) {
        Trainee existing = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        User user = existing.getUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        existing.setDateOfBirth(dateOfBirth);
        existing.setAddress(address);
        user.setIsActive(isActive);
        return traineeRepository.save(existing);
    }

    @Transactional
    public void deleteTrainee(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        traineeRepository.delete(trainee);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return traineeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return traineeRepository.findByUserUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        return traineeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        return traineeRepository.findByUserUsername(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        if (!authenticate(username, currentPassword)) {
            throw new SecurityException("Authentication failed for user: " + username);
        }
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        trainee.getUser().setPassword(newPassword);
        traineeRepository.save(trainee);
    }

    @Transactional
    public void activate(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        if (Boolean.TRUE.equals(trainee.getUser().getIsActive())) {
            throw new IllegalStateException("Trainee is already active: " + username);
        }
        trainee.getUser().setIsActive(true);
        traineeRepository.save(trainee);
    }

    @Transactional
    public void deactivate(String username, String password) {
        if (!authenticate(username, password)) {
            throw new SecurityException("Authentication failed for user: " + username);
        }
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        if (Boolean.FALSE.equals(trainee.getUser().getIsActive())) {
            throw new IllegalStateException("Trainee is already inactive: " + username);
        }
        trainee.getUser().setIsActive(false);
        traineeRepository.save(trainee);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                       String trainerName, String trainingTypeName) {
        return trainingRepository.findAll(
                TrainingSpecification.byTraineeCriteria(username, fromDate, toDate, trainerName, trainingTypeName));
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedTrainers(String traineeUsername) {
        return trainerRepository.findAll(TrainerSpecification.notAssignedToTrainee(traineeUsername));
    }

    @Transactional
    public Trainee updateTrainers(String traineeUsername, Set<String> trainerUsernames) {
        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + traineeUsername));
        List<Trainer> newTrainers = trainerRepository.findByUserUsernameIn(trainerUsernames);
        if (trainerUsernames.size() != newTrainers.size()) {
            throw new NoSuchElementException("Trainers not found: " + getUnknownNames(trainerUsernames, newTrainers));
        }
        trainee.getTrainers().removeIf(t -> !trainerUsernames.contains(t.getUser().getUsername()));
        Set<String> currentUsernames = trainee.getTrainers().stream()
                .map(t -> t.getUser().getUsername())
                .collect(Collectors.toSet());
        trainee.getTrainers().addAll(newTrainers.stream()
                .filter(t -> !currentUsernames.contains(t.getUser().getUsername()))
                .toList());
        return traineeRepository.save(trainee);
    }

    // Helpers

    private List<String> getUnknownNames(Set<String> trainerUsernames, List<Trainer> foundTrainers) {
        Set<String> foundUsernames = foundTrainers.stream()
                .map(t -> t.getUser().getUsername())
                .collect(Collectors.toSet());
        return trainerUsernames.stream()
                .filter(u -> !foundUsernames.contains(u))
                .toList();
    }

    private Trainee buildTraineeForCreate(String firstName, String lastName,
                                          LocalDate dateOfBirth, String address) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(usernameGenerator.generateUsername(firstName, lastName))
                .password(passwordGenerator.generatePassword())
                .isActive(true)
                .build();
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        return trainee;
    }
}
