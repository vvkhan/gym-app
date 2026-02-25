package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainingTypeDao;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

@LogExecution
@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Optional<TrainingType> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(TrainingType.class, id));
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        return sessionFactory.getCurrentSession()
                .createQuery("from TrainingType t where t.trainingTypeName = :n", TrainingType.class)
                .setParameter("n", name)
                .uniqueResultOptional();
    }

    @Override
    public List<TrainingType> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from TrainingType", TrainingType.class)
                .list();
    }
}
