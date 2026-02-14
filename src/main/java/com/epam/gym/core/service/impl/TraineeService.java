package com.epam.gym.core.service.impl;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.exception.EntityNotFoundException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.service.AbstractService;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService extends AbstractService<Trainee, Long> {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;

    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        Trainee trainee = buildTraineeForCreate(firstName, lastName, dateOfBirth, address);
        return createEntity(trainee);
    }

    public Trainee updateTrainee(Long id, String firstName, String lastName, LocalDate dateOfBirth,
                                String address, Boolean isActive) {
        Trainee trainee = buildTraineeForUpdate(firstName, lastName, dateOfBirth, address, isActive);
        return updateEntity(id, trainee);
    }

    public void deleteTrainee(Long id) {
        deleteEntity(id);
    }

    public Optional<Trainee> getTraineeById(Long id) {
        return getById(id);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        return getByUsername(username);
    }

    public List<Trainee> getAllTrainees() {
        return getAll();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getEntityName() {
        return "trainee";
    }

    @Override
    protected Long getEntityId(Trainee entity) {
        return entity.getId();
    }

    @Override
    protected Trainee doCreate(Trainee entity) {
        return traineeDao.create(entity);
    }

    @Override
    protected Trainee doUpdate(Long id, Trainee entity) {
        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", id));

        existing.setFirstName(entity.getFirstName());
        existing.setLastName(entity.getLastName());
        existing.setDateOfBirth(entity.getDateOfBirth());
        existing.setAddress(entity.getAddress());
        existing.setIsActive(entity.getIsActive());

        return traineeDao.update(existing);
    }

    @Override
    protected void doDelete(Long id) {
        traineeDao.delete(id);
    }

    @Override
    protected Trainee doGet(Long id) {
        return traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", id));
    }

    // DAO methods

    @Override
    protected Optional<Trainee> daoFindById(Long id) {
        return traineeDao.findById(id);
    }

    @Override
    protected List<Trainee> daoFindAll() {
        return traineeDao.findAll();
    }

    @Override
    protected Optional<Trainee> daoFindByUsername(String username) {
        return traineeDao.findByUsername(username);
    }

    // Helpers

    private Trainee buildTraineeForCreate(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setIsActive(true);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        return trainee;
    }

    private Trainee buildTraineeForUpdate(String firstName, String lastName, LocalDate dateOfBirth,
                                         String address, Boolean isActive) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setIsActive(isActive);

        return trainee;
    }
}
