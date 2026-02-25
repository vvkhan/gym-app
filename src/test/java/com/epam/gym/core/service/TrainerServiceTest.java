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
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer trainerWithUser(String username, String password, boolean isActive) {
        User user = User.builder()
                .firstName("John").lastName("Smith")
                .username(username).password(password).isActive(isActive)
                .build();
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Fitness");
        return Trainer.builder().user(user).specialization(type).build();
    }

    private TrainingType trainingType(Long id, String name) {
        TrainingType t = new TrainingType();
        t.setId(id);
        t.setTrainingTypeName(name);
        return t;
    }

    @Test
    void createTrainer_BuildUserAndTrainer() {
        TrainingType type = trainingType(1L, "Cardio");
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(type));
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");

        Trainer saved = trainerWithUser("John.Doe", "pass123", true);
        when(trainerDao.create(any(Trainer.class))).thenReturn(saved);

        Trainer result = trainerService.createTrainer("John", "Doe", 1L);

        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUsername());
        assertEquals("pass123", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());
        verify(trainerDao).create(any(Trainer.class));
    }

    @Test
    void createTrainer_ThrowWhenTrainingTypeNotFound() {
        when(trainingTypeDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.createTrainer("John", "Doe", 999L));
        verify(trainerDao, never()).create(any());
    }

    @Test
    void updateTrainer_UpdateUserFields() {
        TrainingType newType = trainingType(2L, "Yoga");
        Trainer existing = trainerWithUser("John.Smith", "pass", true);

        when(trainingTypeDao.findById(2L)).thenReturn(Optional.of(newType));
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(existing));
        when(trainerDao.update(existing)).thenReturn(existing);

        Trainer result = trainerService.updateTrainer("John.Smith", "Jane", "Doe", 2L, false);

        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertFalse(result.getUser().getIsActive());
        assertEquals("Yoga", result.getSpecialization().getTrainingTypeName());
        verify(trainerDao).update(existing);
    }

    @Test
    void updateTrainer_ThrowWhenTrainerNotFound() {
        TrainingType type = trainingType(1L, "Fitness");
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(type));
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.updateTrainer("unknown", "A", "B", 1L, true));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void updateTrainer_ThrowWhenTrainingTypeNotFound() {
        when(trainingTypeDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.updateTrainer("John.Smith", "A", "B", 999L, true));
        verify(trainerDao, never()).findByUsername(any());
        verify(trainerDao, never()).update(any());
    }

    @Test
    void getTrainerById_ReturnEmptyWhenIdIsNull() {
        assertFalse(trainerService.getTrainerById(null).isPresent());
        verify(trainerDao, never()).findById(any());
    }

    @Test
    void getTrainerByUsername_ReturnEmptyWhenUsernameIsNull() {
        assertFalse(trainerService.getTrainerByUsername(null).isPresent());
        verify(trainerDao, never()).findByUsername(any());
    }

    @Test
    void authenticate_ReturnTrueWhenCredentialsMatch() {
        Trainer trainer = trainerWithUser("John.Smith", "secret", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertTrue(trainerService.authenticate("John.Smith", "secret"));
    }

    @Test
    void authenticate_ReturnFalseWhenPasswordWrong() {
        Trainer trainer = trainerWithUser("John.Smith", "secret", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertFalse(trainerService.authenticate("John.Smith", "wrong"));
    }

    @Test
    void authenticate_ReturnFalseWhenUserNotFound() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertFalse(trainerService.authenticate("unknown", "any"));
    }

    @Test
    void changePassword_UpdatePassword() {
        Trainer trainer = trainerWithUser("John.Smith", "oldPass", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(trainer)).thenReturn(trainer);

        trainerService.changePassword("John.Smith", "oldPass", "newPass");

        assertEquals("newPass", trainer.getUser().getPassword());
        verify(trainerDao).update(trainer);
    }

    @Test
    void changePassword_ThrowWhenCurrentPasswordIncorrect() {
        Trainer trainer = trainerWithUser("John.Smith", "correct", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(IllegalArgumentException.class, () ->
                trainerService.changePassword("John.Smith", "wrong", "newPass"));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void activate_SetIsActiveTrue() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", false);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(trainer)).thenReturn(trainer);

        trainerService.activate("John.Smith");

        assertTrue(trainer.getUser().getIsActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void activate_ThrowWhenAlreadyActive() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class, () -> trainerService.activate("John.Smith"));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void deactivate_SetIsActiveFalse() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(trainer)).thenReturn(trainer);

        trainerService.deactivate("John.Smith");

        assertFalse(trainer.getUser().getIsActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void deactivate_ThrowWhenAlreadyInactive() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", false);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class, () -> trainerService.deactivate("John.Smith"));
        verify(trainerDao, never()).update(any());
    }

}
