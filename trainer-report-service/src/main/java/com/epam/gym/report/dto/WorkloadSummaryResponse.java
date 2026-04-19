package com.epam.gym.report.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WorkloadSummaryResponse {

    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private List<YearSummary> years;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class YearSummary {
        private int year;
        private List<MonthSummary> months;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MonthSummary {
        private int month;
        private int totalDurationMinutes;
    }
}
