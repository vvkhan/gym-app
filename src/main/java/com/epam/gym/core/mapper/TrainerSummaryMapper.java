package com.epam.gym.core.mapper;

import com.epam.gym.core.dto.response.TrainerSummaryResponse;
import com.epam.gym.core.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TrainingTypeMapper.class)
public interface TrainerSummaryMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    TrainerSummaryResponse toDto(Trainer trainer);
}
