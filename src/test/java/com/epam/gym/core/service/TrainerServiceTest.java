package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.TrainingTypeRepository;
import org.springframework.data.jpa.domain.Specification;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TrainingRepository trainingRepository;

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
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);
        return trainer;
    }

    private TrainingType trainingType(UUID id, String name) {
        TrainingType t = new TrainingType();
        t.setId(id);
        t.setTrainingTypeName(name);
        return t;
    }

    @Test
    void createTrainer_BuildUserAndTrainer() {
        UUID typeId = UUID.randomUUID();
        TrainingType type = trainingType(typeId, "Cardio");
        when(trainingTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");

        Trainer saved = trainerWithUser("John.Doe", "pass123", true);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(saved);

        Trainer result = trainerService.createTrainer("John", "Doe", typeId);

        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUsername());
        assertEquals("pass123", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void createTrainer_ThrowWhenTrainingTypeNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(trainingTypeRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.createTrainer("John", "Doe", unknownId));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateTrainer_UpdateUserFields() {
        UUID typeId = UUID.randomUUID();
        TrainingType newType = trainingType(typeId, "Yoga");
        Trainer existing = trainerWithUser("John.Smith", "pass", true);

        when(trainingTypeRepository.findById(typeId)).thenReturn(Optional.of(newType));
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(existing));
        when(trainerRepository.save(existing)).thenReturn(existing);

        Trainer result = trainerService.updateTrainer("John.Smith", "Jane", "Doe", typeId, false);

        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertFalse(result.getUser().getIsActive());
        assertEquals("Yoga", result.getSpecialization().getTrainingTypeName());
        verify(trainerRepository).save(existing);
    }

    @Test
    void updateTrainer_ThrowWhenTrainerNotFound() {
        UUID typeId = UUID.randomUUID();
        TrainingType type = trainingType(typeId, "Fitness");
        when(trainingTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(trainerRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.updateTrainer("unknown", "A", "B", typeId, true));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateTrainer_ThrowWhenTrainingTypeNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(trainingTypeRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.updateTrainer("John.Smith", "A", "B", unknownId, true));
        verify(trainerRepository, never()).findByUserUsername(any());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getTrainerById_ReturnEmptyWhenIdIsNull() {
        assertFalse(trainerService.getTrainerById(null).isPresent());
        verify(trainerRepository, never()).findById(any());
    }

    @Test
    void getTrainerByUsername_ReturnEmptyWhenUsernameIsNull() {
        assertFalse(trainerService.getTrainerByUsername(null).isPresent());
        verify(trainerRepository, never()).findByUserUsername(any());
    }

    @Test
    void authenticate_ReturnTrueWhenCredentialsMatch() {
        Trainer trainer = trainerWithUser("John.Smith", "secret", true);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertTrue(trainerService.authenticate("John.Smith", "secret"));
    }

    @Test
    void authenticate_ReturnFalseWhenPasswordWrong() {
        Trainer trainer = trainerWithUser("John.Smith", "secret", true);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertFalse(trainerService.authenticate("John.Smith", "wrong"));
    }

    @Test
    void authenticate_ReturnFalseWhenUserNotFound() {
        when(trainerRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertFalse(trainerService.authenticate("unknown", "any"));
    }

    @Test
    void changePassword_UpdatePassword() {
        Trainer trainer = trainerWithUser("John.Smith", "oldPass", true);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.changePassword("John.Smith", "oldPass", "newPass");

        assertEquals("newPass", trainer.getUser().getPassword());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void changePassword_ThrowWhenCredentialsInvalid() {
        Trainer trainer = trainerWithUser("John.Smith", "correct", true);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(SecurityException.class, () ->
                trainerService.changePassword("John.Smith", "wrong", "newPass"));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void activate_SetIsActiveTrue() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", false);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.activate("John.Smith");

        assertTrue(trainer.getUser().getIsActive());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void activate_ThrowWhenAlreadyActive() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", true);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class, () -> trainerService.activate("John.Smith"));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void deactivate_SetIsActiveFalse() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", true);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.deactivate("John.Smith", "pass");

        assertFalse(trainer.getUser().getIsActive());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void deactivate_ThrowWhenAlreadyInactive() {
        Trainer trainer = trainerWithUser("John.Smith", "pass", false);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class, () -> trainerService.deactivate("John.Smith", "pass"));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void deactivate_ThrowWhenCredentialsInvalid() {
        Trainer trainer = trainerWithUser("John.Smith", "correct", true);
        when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(SecurityException.class, () -> trainerService.deactivate("John.Smith", "wrong"));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getTrainings_DelegateToTrainingRepository() {
        Training training = new Training();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));

        List<Training> result = trainerService.getTrainings(
                "John.Smith", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "John.Doe");

        assertEquals(1, result.size());
        verify(trainingRepository).findAll(any(Specification.class));
    }
}
