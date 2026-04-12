package com.epam.gym.report.controller;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.dto.WorkloadSummaryResponse;
import com.epam.gym.report.exception.GlobalExceptionHandler;
import com.epam.gym.report.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerReportControllerTest {

    @Mock
    ReportService reportService;

    @InjectMocks
    TrainerReportController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void updateWorkload_validRequest_returns200() throws Exception {
        mockMvc.perform(post("/api/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk());

        verify(reportService).processWorkload(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateWorkload_missingRequiredField_returns400() throws Exception {
        WorkloadRequest invalid = new WorkloadRequest();
        invalid.setTrainingDate(LocalDate.of(2024, 3, 15));
        invalid.setTrainingDurationMinutes(60);
        invalid.setActionType(ActionType.ADD);
        // trainerUsername intentionally left blank

        mockMvc.perform(post("/api/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_existingTrainer_returns200WithBody() throws Exception {
        WorkloadSummaryResponse summary = new WorkloadSummaryResponse();
        summary.setUsername("john.doe");
        summary.setFirstName("John");
        summary.setLastName("Doe");
        summary.setActive(true);
        summary.setYears(List.of());
        when(reportService.getSummary("john.doe")).thenReturn(summary);

        mockMvc.perform(get("/api/report/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"));
    }

    @Test
    void getSummary_unknownTrainer_returns404() throws Exception {
        when(reportService.getSummary("unknown"))
                .thenThrow(new NoSuchElementException("No workload data for trainer: unknown"));

        mockMvc.perform(get("/api/report/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMonthlyDuration_returns200WithValue() throws Exception {
        when(reportService.getMonthlyDuration("john.doe", 2024, 3)).thenReturn(120);

        mockMvc.perform(get("/api/report/john.doe/2024/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(120));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private WorkloadRequest validRequest() {
        WorkloadRequest r = new WorkloadRequest();
        r.setTrainerUsername("john.doe");
        r.setFirstName("John");
        r.setLastName("Doe");
        r.setActive(true);
        r.setTrainingDate(LocalDate.of(2024, 3, 15));
        r.setTrainingDurationMinutes(60);
        r.setActionType(ActionType.ADD);
        return r;
    }
}
