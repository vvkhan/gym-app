package com.epam.gym.core.service;

import com.epam.gym.core.dao.TraineeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createTrainee_GenerateCredentialsAndCreateTrainee() {
        String firstName = "John";
        String lastName = "Doe";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        String address = "123 Main St";

        when(usernameGenerator.generateUsername(firstName, lastName)).thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("password123");

        Trainee savedTrainee = new Trainee();
        savedTrainee.setId(1L);
        savedTrainee.setFirstName(firstName);
        savedTrainee.setLastName(lastName);
        savedTrainee.setUsername("John.Doe");
        savedTrainee.setPassword("password123");
        savedTrainee.setIsActive(true);
        savedTrainee.setDateOfBirth(dateOfBirth);
        savedTrainee.setAddress(address);

        when(traineeDao.create(any(Trainee.class))).thenReturn(savedTrainee);

        Trainee result = traineeService.createTrainee(firstName, lastName, dateOfBirth, address);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John.Doe", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertEquals(true, result.getIsActive());
        assertEquals(dateOfBirth, result.getDateOfBirth());
        assertEquals(address, result.getAddress());

        verify(usernameGenerator).generateUsername(firstName, lastName);
        verify(passwordGenerator).generatePassword();
        verify(traineeDao).create(any(Trainee.class));
    }

    @Test
    void updateTrainee_UpdateExistingTrainee() {
        Long id = 1L;
        String firstName = "Jane";
        String lastName = "Doe";
        LocalDate dateOfBirth = LocalDate.of(1995, 5, 15);
        String address = "456 Oak Ave";
        Boolean isActive = false;

        Trainee existingTrainee = new Trainee();
        existingTrainee.setId(id);
        existingTrainee.setFirstName("John");
        existingTrainee.setLastName("Smith");

        when(traineeDao.findById(id)).thenReturn(Optional.of(existingTrainee));

        Trainee updatedTrainee = new Trainee();
        updatedTrainee.setId(id);
        updatedTrainee.setFirstName(firstName);
        updatedTrainee.setLastName(lastName);
        updatedTrainee.setDateOfBirth(dateOfBirth);
        updatedTrainee.setAddress(address);
        updatedTrainee.setIsActive(isActive);

        when(traineeDao.update(any(Trainee.class))).thenReturn(updatedTrainee);

        Trainee result = traineeService.updateTrainee(id, firstName, lastName, dateOfBirth, address, isActive);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertEquals(dateOfBirth, result.getDateOfBirth());
        assertEquals(address, result.getAddress());
        assertEquals(isActive, result.getIsActive());

        verify(traineeDao).findById(id);
        verify(traineeDao).update(any(Trainee.class));
    }

    @Test
    void updateTrainee_ThrowExceptionWhenTraineeNotFound() {
        Long id = 999L;
        when(traineeDao.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                traineeService.updateTrainee(id, "John", "Doe", LocalDate.now(), "Address", true)
        );

        verify(traineeDao).findById(id);
        verify(traineeDao, never()).update(any(Trainee.class));
    }

    @Test
    void deleteTrainee_CallDaoDelete() {
        Long id = 1L;
        doNothing().when(traineeDao).delete(id);

        traineeService.deleteTrainee(id);

        verify(traineeDao).delete(id);
    }

    @Test
    void deleteTrainee_ThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.deleteTrainee(null));
        verify(traineeDao, never()).delete(any());
    }

    @Test
    void updateTrainee_ThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                traineeService.updateTrainee(null, "John", "Doe", LocalDate.now(), "Address", true)
        );
        verify(traineeDao, never()).findById(any());
        verify(traineeDao, never()).update(any());
    }

    @Test
    void getTraineeById_DelegatesToDao() {
        traineeService.getTraineeById(1L);
        verify(traineeDao).findById(1L);
    }

    @Test
    void getTraineeById_ReturnEmptyWhenIdIsNull() {
        Optional<Trainee> result = traineeService.getTraineeById(null);

        assertFalse(result.isPresent());
        verify(traineeDao, never()).findById(any());
    }

    @Test
    void getTraineeByUsername_DelegatesToDao() {
        traineeService.getTraineeByUsername("John.Doe");
        verify(traineeDao).findByUsername("John.Doe");
    }

    @Test
    void getTraineeByUsername_ReturnEmptyWhenUsernameIsNull() {
        Optional<Trainee> result = traineeService.getTraineeByUsername(null);

        assertFalse(result.isPresent());
        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    void getAllTrainees_DelegatesToDao() {
        traineeService.getAllTrainees();
        verify(traineeDao).findAll();
    }
}
