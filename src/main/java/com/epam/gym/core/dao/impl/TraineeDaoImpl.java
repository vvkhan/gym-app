package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.model.Training;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.hibernate.SessionFactory;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@LogExecution
@Repository
public class TraineeDaoImpl implements TraineeDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Trainee create(Trainee trainee) {
        sessionFactory.getCurrentSession().persist(trainee);
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Trainee.class, id));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        return sessionFactory.getCurrentSession()
                .createQuery("from Trainee t where t.user.username = :u", Trainee.class)
                .setParameter("u", username)
                .uniqueResultOptional();
    }

    @Override
    public List<Trainee> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Trainee", Trainee.class)
                .list();
    }

    @Override
    public Trainee update(Trainee trainee) {
        return sessionFactory.getCurrentSession().merge(trainee);
    }

    @Override
    public void delete(String username) {
        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
        Session session = sessionFactory.getCurrentSession();
        session.remove(session.contains(trainee) ? trainee : session.merge(trainee));
    }

    @Override
    public List<Training> findTrainingsByCriteria(String traineeUsername, LocalDate fromDate,
                                                  LocalDate toDate, String trainerName,
                                                  String trainingTypeName) {
        StringBuilder hql = new StringBuilder("from Training t where t.trainee.user.username = :u");
        if (fromDate!= null) { hql.append(" and t.trainingDate >= :from"); }
        if (toDate!= null) { hql.append(" and t.trainingDate <= :to"); }
        if (trainerName!= null) { hql.append(" and t.trainer.user.username = :trainer"); }
        if (trainingTypeName!= null) { hql.append(" and t.trainingType.trainingTypeName = :type"); }

        var query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("u", traineeUsername);

        if (fromDate != null) { query.setParameter("from", fromDate); }
        if (toDate != null ) { query.setParameter("to", toDate); }
        if (trainerName != null ) { query.setParameter("trainer", trainerName); }
        if (trainingTypeName != null ) { query.setParameter("type", trainingTypeName); }

        return query.list();
    }

    @Override
    public List<String> findUsernamesByPrefix(String prefix) {
        return sessionFactory.getCurrentSession()
                .createQuery("select t.user.username from Trainee t " +
                        "where t.user.username = :exact or t.user.username like :pattern", String.class)
                .setParameter("exact", prefix)
                .setParameter("pattern", prefix + "%")
                .list();
    }
}
