package com.epam.gym.core.mapper;

import com.epam.gym.core.dto.response.TraineeSummaryResponse;
import com.epam.gym.core.model.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeSummaryMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    TraineeSummaryResponse toDto(Trainee trainee);
}
