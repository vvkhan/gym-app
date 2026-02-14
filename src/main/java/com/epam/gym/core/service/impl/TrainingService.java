package com.epam.gym.core.service.impl;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.dao.TrainingDao;
import com.epam.gym.core.dao.TrainingTypeDao;
import com.epam.gym.core.exception.EntityNotFoundException;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.service.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingService extends AbstractService<Training, Long> {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;

    private TraineeDao traineeDao;

    private TrainerDao trainerDao;

    private TrainingTypeDao trainingTypeDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                  Long trainingTypeId, LocalDate trainingDate, Integer duration) {
        // check if trainee exists
        traineeDao.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", traineeId));

        // check if trainer exists
        trainerDao.findById(trainerId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", trainerId));

        // check and get training type
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType", trainingTypeId));

        Training training = buildTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, duration);
        return createEntity(training);
    }

    public Optional<Training> getTrainingById(Long id) {
        return getById(id);
    }

    public List<Training> getAllTrainings() {
        return getAll();
    }

    public List<Training> getTrainingsByTrainee(Long traineeId) {
        log.debug("Getting trainings for trainee: {}", traineeId);
        return trainingDao.findByTraineeId(traineeId);
    }

    public List<Training> getTrainingsByTrainer(Long trainerId) {
        log.debug("Getting trainings for trainer: {}", trainerId);
        return trainingDao.findByTrainerId(trainerId);
    }

    public List<TrainingType> getAllTrainingTypes() {
        log.debug("Getting all training types");
        return trainingTypeDao.findAll();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getEntityName() {
        return "training";
    }

    @Override
    protected Long getEntityId(Training entity) {
        return entity.getId();
    }

    @Override
    protected Training doCreate(Training entity) {
        return trainingDao.create(entity);
    }

    @Override
    protected Training doUpdate(Long id, Training entity) {
        throw new UnsupportedOperationException("Training update is not supported");
    }

    @Override
    protected void doDelete(Long id) {
        throw new UnsupportedOperationException("Training deletion is not supported");
    }

    @Override
    protected Training doGet(Long id) {
        return trainingDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training", id));
    }

    // Helpers

    private Training buildTraining(Long traineeId, Long trainerId, String trainingName,
                                  TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setDuration(duration);

        return training;
    }

    // DAO methods

    @Override
    protected Optional<Training> daoFindById(Long id) {
        return trainingDao.findById(id);
    }

    @Override
    protected List<Training> daoFindAll() {
        return trainingDao.findAll();
    }
}
