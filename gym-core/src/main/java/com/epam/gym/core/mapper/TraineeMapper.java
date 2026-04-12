package com.epam.gym.core.mapper;

import com.epam.gym.core.dto.response.RegistrationResponse;
import com.epam.gym.core.dto.response.TraineeProfileResponse;
import com.epam.gym.core.dto.response.UpdatedTraineeProfileResponse;
import com.epam.gym.core.model.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TrainerSummaryMapper.class)
public interface TraineeMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.rawPassword", target = "password")
    RegistrationResponse toRegistrationResponse(Trainee trainee);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive")
    UpdatedTraineeProfileResponse toUpdatedProfileResponse(Trainee trainee);
}
