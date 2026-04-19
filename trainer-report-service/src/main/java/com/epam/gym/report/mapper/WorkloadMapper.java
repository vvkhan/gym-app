package com.epam.gym.report.mapper;

import com.epam.gym.report.dto.WorkloadSummaryResponse;
import com.epam.gym.report.model.TrainerWorkload;
import com.epam.gym.report.model.TrainingMonth;
import com.epam.gym.report.model.TrainingYear;
import org.mapstruct.Mapper;

@Mapper
public interface WorkloadMapper {

    WorkloadSummaryResponse toResponse(TrainerWorkload workload);

    WorkloadSummaryResponse.YearSummary toYearSummary(TrainingYear year);

    WorkloadSummaryResponse.MonthSummary toMonthSummary(TrainingMonth month);
}
