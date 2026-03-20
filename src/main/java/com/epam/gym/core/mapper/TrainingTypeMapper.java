package com.epam.gym.core.mapper;

import com.epam.gym.core.dto.response.TrainingTypeResponse;
import com.epam.gym.core.model.TrainingType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TrainingTypeMapper {

    TrainingTypeResponse toDto(TrainingType trainingType);

    List<TrainingTypeResponse> toDto(List<TrainingType> trainingTypes);
}
