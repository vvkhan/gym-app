package com.epam.gym.core.dao.impl;

import com.epam.gym.core.model.TrainingType;
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
class TrainingTypeDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<TrainingType> query;

    @InjectMocks
    private TrainingTypeDaoImpl trainingTypeDao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void findById_ReturnTrainingType() {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Fitness");
        when(session.get(TrainingType.class, 1L)).thenReturn(type);

        Optional<TrainingType> result = trainingTypeDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Fitness", result.get().getTrainingTypeName());
    }

    @Test
    void findById_ReturnEmptyWhenNotFound() {
        when(session.get(TrainingType.class, 99L)).thenReturn(null);

        assertFalse(trainingTypeDao.findById(99L).isPresent());
    }

    @Test
    void findById_ThrowWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingTypeDao.findById(null));
        verify(session, never()).get(any(Class.class), any());
    }

    @Test
    void findByName_ReturnTrainingType() {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Yoga");
        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(type));

        Optional<TrainingType> result = trainingTypeDao.findByName("Yoga");

        assertTrue(result.isPresent());
        assertEquals("Yoga", result.get().getTrainingTypeName());
        verify(query).setParameter(eq("n"), eq("Yoga"));
    }

    @Test
    void findByName_ReturnEmptyWhenNotFound() {
        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        assertFalse(trainingTypeDao.findByName("Unknown").isPresent());
    }

    @Test
    void findByName_ThrowWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingTypeDao.findByName(null));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findAll_ReturnList() {
        List<TrainingType> types = List.of(new TrainingType(), new TrainingType(), new TrainingType());
        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.list()).thenReturn(types);

        assertEquals(3, trainingTypeDao.findAll().size());
    }
}
