package com.epam.gym.core.util;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.dao.TrainerDao;
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
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private UsernameGeneratorImpl usernameGenerator;

    @Test
    void generateUsername_ReturnBaseWhenNoConflicts() {
        when(traineeDao.findUsernamesByPrefix("John.Smith")).thenReturn(List.of());
        when(trainerDao.findUsernamesByPrefix("John.Smith")).thenReturn(List.of());

        assertEquals("John.Smith", usernameGenerator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_AppendCounterWhenBaseConflictInTraineeStorage() {
        when(traineeDao.findUsernamesByPrefix("John.Smith")).thenReturn(List.of("John.Smith"));
        when(trainerDao.findUsernamesByPrefix("John.Smith")).thenReturn(List.of());

        assertEquals("John.Smith1", usernameGenerator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_AppendCounterWhenBaseConflictInTrainerStorage() {
        when(traineeDao.findUsernamesByPrefix("Jane.Doe")).thenReturn(List.of());
        when(trainerDao.findUsernamesByPrefix("Jane.Doe")).thenReturn(List.of("Jane.Doe"));

        assertEquals("Jane.Doe1", usernameGenerator.generateUsername("Jane", "Doe"));
    }

    @Test
    void generateUsername_FindNextAvailableWhenConflictsInBothStorages() {
        when(traineeDao.findUsernamesByPrefix("Alice.Johnson")).thenReturn(List.of("Alice.Johnson"));
        when(trainerDao.findUsernamesByPrefix("Alice.Johnson")).thenReturn(List.of("Alice.Johnson1"));

        assertEquals("Alice.Johnson2", usernameGenerator.generateUsername("Alice", "Johnson"));
    }

    @Test
    void generateUsername_SkipAllTakenCounters() {
        when(traineeDao.findUsernamesByPrefix("Bob.Wilson"))
                .thenReturn(List.of("Bob.Wilson", "Bob.Wilson1", "Bob.Wilson2"));
        when(trainerDao.findUsernamesByPrefix("Bob.Wilson")).thenReturn(List.of());

        assertEquals("Bob.Wilson3", usernameGenerator.generateUsername("Bob", "Wilson"));
    }

    @Test
    void generateUsername_ReturnDifferentUsernamesForDifferentNames() {
        when(traineeDao.findUsernamesByPrefix("Michael.Brown")).thenReturn(List.of());
        when(trainerDao.findUsernamesByPrefix("Michael.Brown")).thenReturn(List.of());
        when(traineeDao.findUsernamesByPrefix("Sarah.Davis")).thenReturn(List.of());
        when(trainerDao.findUsernamesByPrefix("Sarah.Davis")).thenReturn(List.of());

        String u1 = usernameGenerator.generateUsername("Michael", "Brown");
        String u2 = usernameGenerator.generateUsername("Sarah", "Davis");

        assertEquals("Michael.Brown", u1);
        assertEquals("Sarah.Davis", u2);
        assertNotEquals(u1, u2);
    }

    @Test
    void generateUsername_QueryBothDaosWithSamePrefix() {
        when(traineeDao.findUsernamesByPrefix("John.Smith")).thenReturn(List.of());
        when(trainerDao.findUsernamesByPrefix("John.Smith")).thenReturn(List.of());

        usernameGenerator.generateUsername("John", "Smith");

        verify(traineeDao).findUsernamesByPrefix("John.Smith");
        verify(trainerDao).findUsernamesByPrefix("John.Smith");
    }
}
