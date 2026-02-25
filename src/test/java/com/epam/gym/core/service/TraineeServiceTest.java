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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

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
        return Trainee.builder().user(user).dateOfBirth(LocalDate.of(1990, 1, 1)).build();
    }

    @Test
    void createTrainee_BuildUserAndTrainee() {
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");

        Trainee saved = traineeWithUser("John.Doe", "pass123", true);
        when(traineeDao.create(any(Trainee.class))).thenReturn(saved);

        Trainee result = traineeService.createTrainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 St");

        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUsername());
        assertEquals("pass123", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());
        verify(traineeDao).create(any(Trainee.class));
    }

    @Test
    void updateTrainee_UpdateUserFields() {
        Trainee existing = traineeWithUser("John.Doe", "pass", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(existing));
        when(traineeDao.update(existing)).thenReturn(existing);

        Trainee result = traineeService.updateTrainee(
                "John.Doe", "Jane", "Smith", LocalDate.of(1995, 5, 15), "456 Ave", false);

        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Smith", result.getUser().getLastName());
        assertFalse(result.getUser().getIsActive());
        verify(traineeDao).update(existing);
    }

    @Test
    void updateTrainee_ThrowWhenNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                traineeService.updateTrainee("unknown", "A", "B", LocalDate.now(), "addr", true));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void getTraineeById_ReturnEmptyWhenIdIsNull() {
        Optional<Trainee> result = traineeService.getTraineeById(null);
        assertFalse(result.isPresent());
        verify(traineeDao, never()).findById(any());
    }

    @Test
    void getTraineeByUsername_ReturnEmptyWhenUsernameIsNull() {
        assertFalse(traineeService.getTraineeByUsername(null).isPresent());
        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    void authenticate_ReturnTrueWhenCredentialsMatch() {
        Trainee trainee = traineeWithUser("John.Doe", "secret", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.authenticate("John.Doe", "secret"));
    }

    @Test
    void authenticate_ReturnFalseWhenPasswordWrong() {
        Trainee trainee = traineeWithUser("John.Doe", "secret", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertFalse(traineeService.authenticate("John.Doe", "wrong"));
    }

    @Test
    void authenticate_ReturnFalseWhenUserNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertFalse(traineeService.authenticate("unknown", "any"));
    }

    @Test
    void changePassword_UpdatePassword() {
        Trainee trainee = traineeWithUser("John.Doe", "oldPass", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        traineeService.changePassword("John.Doe", "oldPass", "newPass");

        assertEquals("newPass", trainee.getUser().getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void changePassword_ThrowWhenCurrentPasswordIncorrect() {
        Trainee trainee = traineeWithUser("John.Doe", "correct", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalArgumentException.class, () ->
                traineeService.changePassword("John.Doe", "wrong", "newPass"));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void changePassword_ThrowWhenTraineeNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                traineeService.changePassword("unknown", "pass", "newPass"));
    }

    @Test
    void activate_SetIsActiveTrue() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", false);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        traineeService.activate("John.Doe");

        assertTrue(trainee.getUser().getIsActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void activate_ThrowWhenAlreadyActive() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class, () -> traineeService.activate("John.Doe"));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void deactivate_SetIsActiveFalse() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        traineeService.deactivate("John.Doe");

        assertFalse(trainee.getUser().getIsActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void deactivate_ThrowWhenAlreadyInactive() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", false);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class, () -> traineeService.deactivate("John.Doe"));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void updateTrainers_ReplaceTrainersList() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        Trainer trainer = Trainer.builder()
                .user(User.builder().username("Jane.Smith").password("p").isActive(true)
                        .firstName("Jane").lastName("Smith").build())
                .build();

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee result = traineeService.updateTrainers("John.Doe", List.of("Jane.Smith"));

        assertEquals(1, result.getTrainers().size());
        assertEquals("Jane.Smith", result.getTrainers().get(0).getUser().getUsername());
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateTrainers_ThrowWhenTrainerNotFound() {
        Trainee trainee = traineeWithUser("John.Doe", "pass", true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                traineeService.updateTrainers("John.Doe", List.of("unknown")));
        verify(traineeDao, never()).update(any());
    }
}
