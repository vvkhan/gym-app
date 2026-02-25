package com.epam.gym.core.dao.impl;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainer> trainerQuery;

    @Mock
    private Query<Training> trainingQuery;

    @Mock
    private Query<String> stringQuery;

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    private Trainer trainerWithUser(String username) {
        User user = User.builder().username(username).firstName("John").lastName("Smith")
                .password("pass").isActive(true).build();
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Fitness");
        return Trainer.builder().user(user).specialization(type).build();
    }

    @Test
    void create_PersistAndReturnTrainer() {
        Trainer trainer = trainerWithUser("John.Smith");
        doNothing().when(session).persist(trainer);

        Trainer result = trainerDao.create(trainer);

        verify(session).persist(trainer);
        assertEquals(trainer, result);
    }

    @Test
    void findById_ReturnTrainer() {
        Trainer trainer = trainerWithUser("John.Smith");
        when(session.get(Trainer.class, 1L)).thenReturn(trainer);

        Optional<Trainer> result = trainerDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void findById_ReturnEmptyWhenNotFound() {
        when(session.get(Trainer.class, 99L)).thenReturn(null);

        assertFalse(trainerDao.findById(99L).isPresent());
    }

    @Test
    void findById_ThrowWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.findById(null));
        verify(session, never()).get(any(Class.class), any());
    }

    @Test
    void findByUsername_ReturnTrainer() {
        Trainer trainer = trainerWithUser("John.Smith");
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter(anyString(), any())).thenReturn(trainerQuery);
        when(trainerQuery.uniqueResultOptional()).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerDao.findByUsername("John.Smith");

        assertTrue(result.isPresent());
        assertEquals("John.Smith", result.get().getUser().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyWhenNotFound() {
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter(anyString(), any())).thenReturn(trainerQuery);
        when(trainerQuery.uniqueResultOptional()).thenReturn(Optional.empty());

        assertFalse(trainerDao.findByUsername("unknown").isPresent());
    }

    @Test
    void findByUsername_ThrowWhenUsernameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.findByUsername(null));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findAll_ReturnList() {
        List<Trainer> trainers = List.of(trainerWithUser("A"), trainerWithUser("B"));
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.list()).thenReturn(trainers);

        assertEquals(2, trainerDao.findAll().size());
    }

    @Test
    void update_ReturnMergedTrainer() {
        Trainer trainer = trainerWithUser("John.Smith");
        when(session.merge(trainer)).thenReturn(trainer);

        Trainer result = trainerDao.update(trainer);

        verify(session).merge(trainer);
        assertEquals(trainer, result);
    }

    @Test
    void findNotAssignedTrainers_ReturnList() {
        List<Trainer> trainers = List.of(trainerWithUser("Jane.Doe"));
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter(anyString(), any())).thenReturn(trainerQuery);
        when(trainerQuery.list()).thenReturn(trainers);

        List<Trainer> result = trainerDao.findNotAssignedTrainers("Alice.Johnson");

        assertEquals(1, result.size());
        verify(trainerQuery).setParameter(eq("u"), eq("Alice.Johnson"));
    }

    @Test
    void findTrainingsByCriteria_ApplyAllParameters() {
        List<Training> trainings = List.of(new Training());
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.setParameter(anyString(), any())).thenReturn(trainingQuery);
        when(trainingQuery.list()).thenReturn(trainings);

        List<Training> result = trainerDao.findTrainingsByCriteria(
                "John.Smith", LocalDate.now(), LocalDate.now(), "Alice.Johnson");

        assertEquals(1, result.size());
        verify(trainingQuery, atLeastOnce()).setParameter(anyString(), any());
    }

    @Test
    void findTrainingsByCriteria_WorkWithNullOptionalParams() {
        List<Training> trainings = List.of(new Training());
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.setParameter(anyString(), any())).thenReturn(trainingQuery);
        when(trainingQuery.list()).thenReturn(trainings);

        List<Training> result = trainerDao.findTrainingsByCriteria(
                "John.Smith", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void findUsernamesByPrefix_ReturnList() {
        List<String> usernames = List.of("John.Smith", "John.Smith1");
        when(session.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter(anyString(), any())).thenReturn(stringQuery);
        when(stringQuery.list()).thenReturn(usernames);

        List<String> result = trainerDao.findUsernamesByPrefix("John.Smith");

        assertEquals(2, result.size());
        verify(stringQuery).setParameter(eq("exact"), eq("John.Smith"));
        verify(stringQuery).setParameter(eq("pattern"), eq("John.Smith%"));
    }
}
