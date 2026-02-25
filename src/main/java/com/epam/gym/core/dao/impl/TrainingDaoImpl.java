package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainingDao;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

@LogExecution
@Repository
public class TrainingDaoImpl implements TrainingDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Training create(Training training) {
        sessionFactory.getCurrentSession().persist(training);
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Training.class, id));
    }

    @Override
    public List<Training> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Training", Training.class)
                .list();
    }

    @Override
    public List<Training> findByTraineeId(Long traineeId) {
        if (traineeId == null) {
            throw new IllegalArgumentException("Trainee id must not be null");
        }
        return sessionFactory.getCurrentSession()
                .createQuery("from Training t where t.trainee.id = :id", Training.class)
                .setParameter("id", traineeId)
                .list();
    }

    @Override
    public List<Training> findByTrainerId(Long trainerId) {
        if (trainerId == null) {
            throw new IllegalArgumentException("Trainer id must not be null");
        }
        return sessionFactory.getCurrentSession()
                .createQuery("from Training t where t.trainer.id = :id", Training.class)
                .setParameter("id", trainerId)
                .list();
    }
}
