package com.epam.gym.core.service;

import com.epam.gym.core.dao.TraineeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@LogExecution
@Service
public class TraineeService {

    @Autowired
    private TraineeDao traineeDao;

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

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeDao.create(buildTraineeForCreate(firstName, lastName, dateOfBirth, address));
    }

    public Trainee updateTrainee(Long id, String firstName, String lastName, LocalDate dateOfBirth,
                                String address, Boolean isActive) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + id + " not found"));
        existing.setFirstName(firstName);
        existing.setLastName(lastName);
        existing.setDateOfBirth(dateOfBirth);
        existing.setAddress(address);
        existing.setIsActive(isActive);
        return traineeDao.update(existing);
    }

    public void deleteTrainee(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        traineeDao.delete(id);
    }

    public Optional<Trainee> getTraineeById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return traineeDao.findById(id);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return traineeDao.findByUsername(username);
    }

    public List<Trainee> getAllTrainees() {
        return traineeDao.findAll();
    }

    // Helper

    private Trainee buildTraineeForCreate(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        return Trainee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(password)
                .isActive(true)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();
    }
}
