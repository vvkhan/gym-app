package com.epam.gym.core.service.impl;

import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.dao.TrainingTypeDao;
import com.epam.gym.core.exception.EntityNotFoundException;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.service.AbstractService;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService extends AbstractService<Trainer, Long> {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;

    private TrainingTypeDao trainingTypeDao;

    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer createTrainer(String firstName, String lastName, Long trainingTypeId) {
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType", trainingTypeId));

        Trainer trainer = buildTrainerForCreate(firstName, lastName, trainingType);
        return createEntity(trainer);
    }

    public Trainer updateTrainer(Long id, String firstName, String lastName, Long trainingTypeId, Boolean isActive) {
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType", trainingTypeId));

        Trainer trainer = buildTrainerForUpdate(firstName, lastName, trainingType, isActive);
        return updateEntity(id, trainer);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        return getById(id);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        return getByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        return getAll();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getEntityName() {
        return "trainer";
    }

    @Override
    protected Long getEntityId(Trainer entity) {
        return entity.getId();
    }

    @Override
    protected Trainer doCreate(Trainer entity) {
        return trainerDao.create(entity);
    }

    @Override
    protected Trainer doUpdate(Long id, Trainer entity) {
        Trainer existing = trainerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", id));

        existing.setFirstName(entity.getFirstName());
        existing.setLastName(entity.getLastName());
        existing.setSpecialization(entity.getSpecialization());
        existing.setIsActive(entity.getIsActive());

        return trainerDao.update(existing);
    }

    @Override
    protected void doDelete(Long id) {
        throw new UnsupportedOperationException("Trainer deletion is not supported");
    }

    @Override
    protected Trainer doGet(Long id) {
        return trainerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", id));
    }

    // Helpers

    private Trainer buildTrainerForCreate(String firstName, String lastName, TrainingType trainingType) {
        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);
        trainer.setSpecialization(trainingType);

        return trainer;
    }

    private Trainer buildTrainerForUpdate(String firstName, String lastName, TrainingType trainingType, Boolean isActive) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(trainingType);
        trainer.setIsActive(isActive);

        return trainer;
    }

    // DAO methods

    @Override
    protected Optional<Trainer> daoFindById(Long id) {
        return trainerDao.findById(id);
    }

    @Override
    protected List<Trainer> daoFindAll() {
        return trainerDao.findAll();
    }

    @Override
    protected Optional<Trainer> daoFindByUsername(String username) {
        return trainerDao.findByUsername(username);
    }
}
