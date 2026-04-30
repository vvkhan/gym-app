package com.epam.gym.report.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_summary")
// Statistically, last name has lower duplication than first name
@CompoundIndex(name = "idx_last_first_name", def = "{'lastName': 1, 'firstName': 1}")
@Getter
@Setter
@NoArgsConstructor
public class TrainerSummary {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private boolean active;

    private List<YearSummary> years = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class YearSummary {

        @Min(1900)
        private int year;

        private List<MonthSummary> months = new ArrayList<>();

        public YearSummary(int year) {
            this.year = year;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MonthSummary {

        @Min(1)
        @Max(12)
        private int month;

        @Min(0)
        private int totalDurationMinutes;

        public MonthSummary(int month) {
            this.month = month;
        }
    }
}
