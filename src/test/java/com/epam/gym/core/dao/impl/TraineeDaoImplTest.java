package com.epam.gym.core.dao.impl;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Training;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainee> traineeQuery;

    @Mock
    private Query<Training> trainingQuery;

    @Mock
    private Query<String> stringQuery;

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    private Trainee traineeWithUser(String username) {
        User user = User.builder().username(username).firstName("John").lastName("Doe")
                .password("pass").isActive(true).build();
        return Trainee.builder().user(user).build();
    }

    @Test
    void create_PersistAndReturnTrainee() {
        Trainee trainee = traineeWithUser("John.Doe");
        doNothing().when(session).persist(trainee);

        Trainee result = traineeDao.create(trainee);

        verify(session).persist(trainee);
        assertEquals(trainee, result);
    }

    @Test
    void findById_ReturnTrainee() {
        Trainee trainee = traineeWithUser("John.Doe");
        when(session.get(Trainee.class, 1L)).thenReturn(trainee);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void findById_ReturnEmptyWhenNotFound() {
        when(session.get(Trainee.class, 99L)).thenReturn(null);

        assertFalse(traineeDao.findById(99L).isPresent());
    }

    @Test
    void findById_ThrowWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.findById(null));
        verify(session, never()).get(any(Class.class), any());
    }

    @Test
    void findByUsername_ReturnTrainee() {
        Trainee trainee = traineeWithUser("John.Doe");
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter(anyString(), any())).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeDao.findByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUser().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyWhenNotFound() {
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter(anyString(), any())).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.empty());

        assertFalse(traineeDao.findByUsername("unknown").isPresent());
    }

    @Test
    void findByUsername_ThrowWhenUsernameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.findByUsername(null));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findAll_ReturnList() {
        List<Trainee> trainees = List.of(traineeWithUser("A"), traineeWithUser("B"));
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.list()).thenReturn(trainees);

        assertEquals(2, traineeDao.findAll().size());
    }

    @Test
    void update_ReturnMergedTrainee() {
        Trainee trainee = traineeWithUser("John.Doe");
        Trainee merged = traineeWithUser("John.Doe");
        when(session.merge(trainee)).thenReturn(merged);

        Trainee result = traineeDao.update(trainee);

        verify(session).merge(trainee);
        assertEquals(merged, result);
    }

    @Test
    void delete_RemoveTrainee() {
        Trainee trainee = traineeWithUser("John.Doe");
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter(anyString(), any())).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.of(trainee));
        when(session.contains(trainee)).thenReturn(true);
        doNothing().when(session).remove(trainee);

        traineeDao.delete("John.Doe");

        verify(session).remove(trainee);
    }

    @Test
    void delete_ThrowWhenNotFound() {
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter(anyString(), any())).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> traineeDao.delete("unknown"));
        verify(session, never()).remove(any());
    }

    @Test
    void findTrainingsByCriteria_ApplyAllParameters() {
        List<Training> trainings = List.of(new Training());
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.setParameter(anyString(), any())).thenReturn(trainingQuery);
        when(trainingQuery.list()).thenReturn(trainings);

        List<Training> result = traineeDao.findTrainingsByCriteria(
                "John.Doe", LocalDate.now(), LocalDate.now(), "John.Smith", "Fitness");

        assertEquals(1, result.size());
        verify(trainingQuery, atLeastOnce()).setParameter(anyString(), any());
    }

    @Test
    void findTrainingsByCriteria_WorkWithNullOptionalParams() {
        List<Training> trainings = List.of(new Training());
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.setParameter(anyString(), any())).thenReturn(trainingQuery);
        when(trainingQuery.list()).thenReturn(trainings);

        List<Training> result = traineeDao.findTrainingsByCriteria(
                "John.Doe", null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void findUsernamesByPrefix_ReturnList() {
        List<String> usernames = List.of("John.Doe", "John.Doe1");
        when(session.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter(anyString(), any())).thenReturn(stringQuery);
        when(stringQuery.list()).thenReturn(usernames);

        List<String> result = traineeDao.findUsernamesByPrefix("John.Doe");

        assertEquals(2, result.size());
        verify(stringQuery).setParameter(eq("exact"), eq("John.Doe"));
        verify(stringQuery).setParameter(eq("pattern"), eq("John.Doe%"));
    }
}
