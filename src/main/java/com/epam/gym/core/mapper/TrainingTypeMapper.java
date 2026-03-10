package com.epam.gym.core.mapper;

import com.epam.gym.core.dto.response.TrainingTypeResponse;
import com.epam.gym.core.model.TrainingType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    TrainingTypeResponse toDto(TrainingType trainingType);
}
