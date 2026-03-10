package com.epam.gym.core.mapper;

import com.epam.gym.core.dto.response.TrainingResponse;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    // Trainee's perspective: partner is the trainer
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainer.user", target = "partnerName", qualifiedByName = "fullName")
    TrainingResponse toTraineeViewResponse(Training training);

    // Trainer's perspective: partner is the trainee
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainee.user", target = "partnerName", qualifiedByName = "fullName")
    TrainingResponse toTrainerViewResponse(Training training);

    @Named("fullName")
    default String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
