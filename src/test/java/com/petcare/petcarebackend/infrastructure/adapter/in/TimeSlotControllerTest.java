package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.dto.TimeSlotResponse;
import com.petcare.petcarebackend.application.exception.NotOfferingOwnerException;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotOverlapException;
import com.petcare.petcarebackend.application.port.in.query.TimeSlotQuery;
import com.petcare.petcarebackend.application.port.in.registry.TimeSlotRegistry;
import com.petcare.petcarebackend.infrastructure.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TimeSlotControllerTest {

    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final String SLOTS_URL = "/services/" + OFFERING_ID + "/slots";

    @Mock
    private TimeSlotRegistry timeSlotRegistry;

    @Mock
    private TimeSlotQuery timeSlotQuery;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TimeSlotController(timeSlotRegistry, timeSlotQuery))
                .setControllerAdvice(new GlobalException())
                .build();
    }

    private String body(String date) {
        return """
                {
                  "date": "%s",
                  "startTime": "10:00",
                  "endTime": "11:00"
                }
                """.formatted(date);
    }

    @Test
    void createsTimeSlot() throws Exception {
        UUID id = UUID.randomUUID();
        when(timeSlotRegistry.createTimeSlot(eq(OFFERING_ID), eq(PROVIDER_ID), any()))
                .thenReturn(new RegisterResponse(id, "ok", LocalDateTime.now()));

        mockMvc.perform(post(SLOTS_URL).principal(PROVIDER_ID::toString).contentType(MediaType.APPLICATION_JSON).content(body("2026-11-10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void rejectsMissingFields() throws Exception {
        mockMvc.perform(post(SLOTS_URL).principal(PROVIDER_ID::toString).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(timeSlotRegistry, never()).createTimeSlot(any(), any(), any());
    }

    @Test
    void rejectsMalformedDate() throws Exception {
        mockMvc.perform(post(SLOTS_URL).principal(PROVIDER_ID::toString).contentType(MediaType.APPLICATION_JSON).content(body("10/11/2026")))
                .andExpect(status().isBadRequest());
        verify(timeSlotRegistry, never()).createTimeSlot(any(), any(), any());
    }

    @Test
    void returnsBadRequestWhenTheDomainRejectsTheSlot() throws Exception {
        when(timeSlotRegistry.createTimeSlot(eq(OFFERING_ID), eq(PROVIDER_ID), any()))
                .thenThrow(new IllegalArgumentException("Time slots must end by 17:00"));

        mockMvc.perform(post(SLOTS_URL).principal(PROVIDER_ID::toString).contentType(MediaType.APPLICATION_JSON).content(body("2026-11-10")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsForbiddenForAnotherProvidersOffering() throws Exception {
        when(timeSlotRegistry.createTimeSlot(eq(OFFERING_ID), eq(PROVIDER_ID), any())).thenThrow(new NotOfferingOwnerException());

        mockMvc.perform(post(SLOTS_URL).principal(PROVIDER_ID::toString).contentType(MediaType.APPLICATION_JSON).content(body("2026-11-10")))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsNotFoundForUnknownOfferingWhenCreating() throws Exception {
        when(timeSlotRegistry.createTimeSlot(eq(OFFERING_ID), eq(PROVIDER_ID), any())).thenThrow(new OfferingNotFoundException(OFFERING_ID));

        mockMvc.perform(post(SLOTS_URL).principal(PROVIDER_ID::toString).contentType(MediaType.APPLICATION_JSON).content(body("2026-11-10")))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictForOverlappingSlot() throws Exception {
        when(timeSlotRegistry.createTimeSlot(eq(OFFERING_ID), eq(PROVIDER_ID), any())).thenThrow(new TimeSlotOverlapException());

        mockMvc.perform(post(SLOTS_URL).principal(PROVIDER_ID::toString).contentType(MediaType.APPLICATION_JSON).content(body("2026-11-10")))
                .andExpect(status().isConflict());
    }

    @Test
    void listsFreeSlots() throws Exception {
        UUID id = UUID.randomUUID();
        when(timeSlotQuery.listFreeTimeSlots(OFFERING_ID)).thenReturn(List.of(
                new TimeSlotResponse(id, LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), LocalTime.of(11, 0))));

        mockMvc.perform(get(SLOTS_URL + "/free"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].date").value("2026-11-10"));
    }

    @Test
    void returnsNotFoundForUnknownOfferingWhenListing() throws Exception {
        when(timeSlotQuery.listFreeTimeSlots(OFFERING_ID)).thenThrow(new OfferingNotFoundException(OFFERING_ID));

        mockMvc.perform(get(SLOTS_URL + "/free"))
                .andExpect(status().isNotFound());
    }
}
