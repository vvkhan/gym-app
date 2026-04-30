package com.epam.gym.report.mapper;

import com.epam.gym.report.dto.WorkloadSummaryResponse;
import com.epam.gym.report.model.TrainerSummary;
import org.mapstruct.Mapper;

@Mapper
public interface WorkloadMapper {

    WorkloadSummaryResponse toResponse(TrainerSummary summary);

    WorkloadSummaryResponse.YearSummary toYearSummary(TrainerSummary.YearSummary year);

    WorkloadSummaryResponse.MonthSummary toMonthSummary(TrainerSummary.MonthSummary month);
}
