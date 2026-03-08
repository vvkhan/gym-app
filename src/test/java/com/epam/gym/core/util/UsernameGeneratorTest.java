package com.epam.gym.core.util;

import com.epam.gym.core.repository.TraineeRepository;
import com.epam.gym.core.repository.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private UsernameGeneratorImpl usernameGenerator;

    @Test
    void generateUsername_ReturnBaseWhenNoConflicts() {
        when(traineeRepository.findUsernamesByPrefix("John.Smith", "John.Smith%")).thenReturn(List.of());
        when(trainerRepository.findUsernamesByPrefix("John.Smith", "John.Smith%")).thenReturn(List.of());

        assertEquals("John.Smith", usernameGenerator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_AppendCounterWhenBaseConflictInTraineeStorage() {
        when(traineeRepository.findUsernamesByPrefix("John.Smith", "John.Smith%")).thenReturn(List.of("John.Smith"));
        when(trainerRepository.findUsernamesByPrefix("John.Smith", "John.Smith%")).thenReturn(List.of());

        assertEquals("John.Smith1", usernameGenerator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_AppendCounterWhenBaseConflictInTrainerStorage() {
        when(traineeRepository.findUsernamesByPrefix("Jane.Doe", "Jane.Doe%")).thenReturn(List.of());
        when(trainerRepository.findUsernamesByPrefix("Jane.Doe", "Jane.Doe%")).thenReturn(List.of("Jane.Doe"));

        assertEquals("Jane.Doe1", usernameGenerator.generateUsername("Jane", "Doe"));
    }

    @Test
    void generateUsername_FindNextAvailableWhenConflictsInBothStorages() {
        when(traineeRepository.findUsernamesByPrefix("Alice.Johnson", "Alice.Johnson%")).thenReturn(List.of("Alice.Johnson"));
        when(trainerRepository.findUsernamesByPrefix("Alice.Johnson", "Alice.Johnson%")).thenReturn(List.of("Alice.Johnson1"));

        assertEquals("Alice.Johnson2", usernameGenerator.generateUsername("Alice", "Johnson"));
    }

    @Test
    void generateUsername_SkipAllTakenCounters() {
        when(traineeRepository.findUsernamesByPrefix("Bob.Wilson", "Bob.Wilson%"))
                .thenReturn(List.of("Bob.Wilson", "Bob.Wilson1", "Bob.Wilson2"));
        when(trainerRepository.findUsernamesByPrefix("Bob.Wilson", "Bob.Wilson%")).thenReturn(List.of());

        assertEquals("Bob.Wilson3", usernameGenerator.generateUsername("Bob", "Wilson"));
    }

    @Test
    void generateUsername_ReturnDifferentUsernamesForDifferentNames() {
        when(traineeRepository.findUsernamesByPrefix("Michael.Brown", "Michael.Brown%")).thenReturn(List.of());
        when(trainerRepository.findUsernamesByPrefix("Michael.Brown", "Michael.Brown%")).thenReturn(List.of());
        when(traineeRepository.findUsernamesByPrefix("Sarah.Davis", "Sarah.Davis%")).thenReturn(List.of());
        when(trainerRepository.findUsernamesByPrefix("Sarah.Davis", "Sarah.Davis%")).thenReturn(List.of());

        String u1 = usernameGenerator.generateUsername("Michael", "Brown");
        String u2 = usernameGenerator.generateUsername("Sarah", "Davis");

        assertEquals("Michael.Brown", u1);
        assertEquals("Sarah.Davis", u2);
        assertNotEquals(u1, u2);
    }

    @Test
    void generateUsername_QueryBothRepositoriesWithSamePrefix() {
        when(traineeRepository.findUsernamesByPrefix("John.Smith", "John.Smith%")).thenReturn(List.of());
        when(trainerRepository.findUsernamesByPrefix("John.Smith", "John.Smith%")).thenReturn(List.of());

        usernameGenerator.generateUsername("John", "Smith");

        verify(traineeRepository).findUsernamesByPrefix("John.Smith", "John.Smith%");
        verify(trainerRepository).findUsernamesByPrefix("John.Smith", "John.Smith%");
    }
}
