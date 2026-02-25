package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.hibernate.SessionFactory;
import com.epam.gym.core.model.Training;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@LogExecution
@Repository
public class TrainerDaoImpl implements TrainerDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Trainer create(Trainer trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Trainer.class, id));
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        return sessionFactory.getCurrentSession()
                .createQuery("from Trainer t where t.user.username = :u", Trainer.class)
                .setParameter("u", username)
                .uniqueResultOptional();
    }

    @Override
    public List<Trainer> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Trainer", Trainer.class)
                .list();
    }

    @Override
    public Trainer update(Trainer trainer) {
        return sessionFactory.getCurrentSession().merge(trainer);
    }

    @Override
    public List<Trainer> findNotAssignedTrainers(String traineeUsername) {
        return sessionFactory.getCurrentSession()
                .createQuery("from Trainer t where t not in (" +
                        "select tr from Trainee te join te.trainers tr " +
                        "where te.user.username = :u)", Trainer.class)
                .setParameter("u", traineeUsername)
                .list();
    }

    @Override
    public List<Training> findTrainingsByCriteria(String trainerUsername, LocalDate fromDate,
                                                  LocalDate toDate, String traineeName) {
        StringBuilder hql = new StringBuilder("from Training t where t.trainer.user.username = :u");
        if (fromDate != null) { hql.append(" and t.trainingDate >= :from"); }
        if (toDate != null) { hql.append(" and t.trainingDate <= :to"); }
        if (traineeName != null) { hql.append(" and t.trainee.user.username = :trainee"); }

        var query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("u", trainerUsername);

        if (fromDate != null) { query.setParameter("from", fromDate); }
        if (toDate != null ) { query.setParameter("to", toDate); }
        if (traineeName != null ) { query.setParameter("trainee", traineeName); }

        return query.list();
    }

    @Override
    public List<String> findUsernamesByPrefix(String prefix) {
        return sessionFactory.getCurrentSession()
                .createQuery("select t.user.username from Trainer t " +
                        "where t.user.username = :exact or t.user.username like :pattern", String.class)
                .setParameter("exact", prefix)
                .setParameter("pattern", prefix + "%")
                .list();
    }
}
