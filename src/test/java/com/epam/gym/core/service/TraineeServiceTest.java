package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.TraineeRepository;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.TrainingRepository;
import org.springframework.data.jpa.domain.Specification;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee traineeWithUser(String username, String password, boolean isActive) {
        User user = User.builder()
                .firstName("John").lastName("Doe")
                .username(username).password(password).isActive(isActive)
                .build();
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return trainee;
    }

    @Test
    void createTrainee_BuildUserAndTrainee() {
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");

        Trainee saved = traineeWithUser("John.Doe", "pass123", true);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(saved);

        Trainee result = traineeService.createTrainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 St");

        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUsername());
        assertEquals("pass123", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_UpdateUserFields() {
        Trainee existing = traineeWithUser("John.Doe", "pass", true);
        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(existing));
        when(traineeRepository.save(existing)).thenReturn(existing);

        Trainee result = traineeService.updateTrainee(
                "John.Doe", "Jane", "Smith", LocalDate.of(1995, 5, 15), "456 Ave", false);

        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Smith", result.getUser().getLastName());
        assertFalse(result.getUser().getIsActive());
        verify(traineeRepository).save(existing);
    }

    @Test
    void updateTrainee_ThrowWhenNotFound() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                traineeService.updateTrainee("unknown", "A", "B", LocalDate.now(), "addr", true));
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void getTraineeById_ReturnEmptyWhenIdIsNull() {
        Optional<Trainee> result = traineeService.getTraineeById(null);
        assertFalse(result.isPresent());
        verify(traineeRepository, never()).findById(any());
    }

    @Test
    void getTraineeByUsername_ReturnEmptyWhenUsernameIsNull() {
        assertFalse(traineeService.getTraineeByUsername(null).isPresent());
        verify(traineeRepository, never()).findByUserUsername(any());
    }

    @Test
    void authenticate_ReturnTrueWhenCredentialsMatch() {
        when(traineeRepository.existsByUserUsernameAndUserPassword("John.Doe", "secret")).thenReturn(true);

        assertTrue(traineeService.authenticate("John.Doe", "secret"));
    }

    @Test
    void authenticate_ReturnFalseWhenPasswordWrong() {
        when(traineeRepository.existsByUserUsernameAndUserPassword("John.Doe", "wrong")).thenReturn(false);

        assertFalse(traineeService.authenticate("John.Doe", "wrong"));
    }

    @Test
    void authenticate_ReturnFalseWhenUserNotFound() {
        when(traineeRepository.existsByUserUsernameAndUserPassword("unknown", "any")).thenReturn(false);

        assertFalse(traineeService.authenticate("unknown", "any"));
    }

    @Test
    void changePassword_UpdatePassword() {
        Trainee trainee = traineeWithUser("John.Doe", "oldPass", true);
        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.changePassword("John.Doe", "newPass");

        assertEquals("newPass", trainee.getUser().getPassword());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void activate_SetIsActiveTrue() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", false);
        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.activate("John.Doe");

        assertTrue(trainee.getUser().getIsActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void activate_ThrowWhenAlreadyActive() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class, () -> traineeService.activate("John.Doe"));
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void deactivate_SetIsActiveFalse() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.deactivate("John.Doe");

        assertFalse(trainee.getUser().getIsActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void deactivate_ThrowWhenAlreadyInactive() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", false);
        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class, () -> traineeService.deactivate("John.Doe"));
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateTrainers_UpdateTrainersList() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        trainee.setTrainers(new HashSet<>());
        User trainerUser = User.builder()
                .username("Jane.Smith").password("p").isActive(true)
                .firstName("Jane").lastName("Smith").build();
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsernameIn(Set.of("Jane.Smith"))).thenReturn(List.of(trainer));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        Trainee result = traineeService.updateTrainers("John.Doe", Set.of("Jane.Smith"));

        assertEquals(1, result.getTrainers().size());
        assertEquals("Jane.Smith", result.getTrainers().iterator().next().getUser().getUsername());
        verify(trainerRepository).findByUserUsernameIn(Set.of("Jane.Smith"));
        verify(traineeRepository).save(trainee);
    }

    @Test
    void updateTrainers_ThrowWhenTrainerNotFound() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        when(traineeRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsernameIn(Set.of("unknown"))).thenReturn(List.of());

        assertThrows(NoSuchElementException.class, () ->
                traineeService.updateTrainers("John.Doe", Set.of("unknown")));
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void getTrainings_DelegateToTrainingRepository() {
        Training training = new Training();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));

        List<Training> result = traineeService.getTrainings(
                "John.Doe", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                "Jane.Smith", "Cardio");

        assertEquals(1, result.size());
        verify(trainingRepository).findAll(any(Specification.class));
    }
}
