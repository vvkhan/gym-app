package com.epam.gym.core.dao.impl;

import com.epam.gym.core.model.Training;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Training> trainingQuery;

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void create_PersistAndReturnTraining() {
        Training training = new Training();
        doNothing().when(session).persist(training);

        Training result = trainingDao.create(training);

        verify(session).persist(training);
        assertEquals(training, result);
    }

    @Test
    void findById_ReturnTraining() {
        Training training = new Training();
        when(session.get(Training.class, 1L)).thenReturn(training);

        Optional<Training> result = trainingDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void findById_ReturnEmptyWhenNotFound() {
        when(session.get(Training.class, 99L)).thenReturn(null);

        assertFalse(trainingDao.findById(99L).isPresent());
    }

    @Test
    void findById_ThrowWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.findById(null));
        verify(session, never()).get(any(Class.class), any());
    }

    @Test
    void findAll_ReturnList() {
        List<Training> trainings = List.of(new Training(), new Training());
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.list()).thenReturn(trainings);

        assertEquals(2, trainingDao.findAll().size());
    }

    @Test
    void findByTraineeId_ReturnList() {
        List<Training> trainings = List.of(new Training(), new Training());
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.setParameter(anyString(), any())).thenReturn(trainingQuery);
        when(trainingQuery.list()).thenReturn(trainings);

        List<Training> result = trainingDao.findByTraineeId(1L);

        assertEquals(2, result.size());
        verify(trainingQuery).setParameter(eq("id"), eq(1L));
    }

    @Test
    void findByTraineeId_ThrowWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.findByTraineeId(null));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findByTrainerId_ReturnList() {
        List<Training> trainings = List.of(new Training());
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.setParameter(anyString(), any())).thenReturn(trainingQuery);
        when(trainingQuery.list()).thenReturn(trainings);

        List<Training> result = trainingDao.findByTrainerId(2L);

        assertEquals(1, result.size());
        verify(trainingQuery).setParameter(eq("id"), eq(2L));
    }

    @Test
    void findByTrainerId_ThrowWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.findByTrainerId(null));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }
}
