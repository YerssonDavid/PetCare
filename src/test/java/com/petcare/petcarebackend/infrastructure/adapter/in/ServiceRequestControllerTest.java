package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.DuplicateServiceRequestException;
import com.petcare.petcarebackend.application.exception.PetNotFoundException;
import com.petcare.petcarebackend.application.exception.PetNotOwnedException;
import com.petcare.petcarebackend.application.exception.TimeSlotNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotUnavailableException;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestRegistry;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.infrastructure.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceRequestControllerTest {

    private static final UUID ID = UUID.randomUUID();

    @Mock
    private ServiceRequestRegistry serviceRequestRegistry;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceRequestController(serviceRequestRegistry))
                .setControllerAdvice(new GlobalException())
                .build();
    }

    private ResultActions postRequest(String purchaseType) throws Exception {
        String body = """
                {
                  "ownerId": "%s",
                  "petId": "%s",
                  "slotId": "%s",
                  "purchaseType": %s
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), purchaseType);
        return mockMvc.perform(post("/service-requests").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void createsRequestAndReturnsConfirmation() throws Exception {
        when(serviceRequestRegistry.createServiceRequest(any())).thenReturn(new ServiceRequestResponse(
                ID, "Firulais", "Baño completo", OfferingType.SERVICIO, "Vet Sol",
                LocalDate.of(2026, 11, 11), LocalTime.of(10, 0), LocalTime.of(11, 0),
                new BigDecimal("45000.00"), RequestStatus.PENDIENTE, null, null, LocalDateTime.of(2026, 11, 10, 8, 0)));

        postRequest("null")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.petName").value("Firulais"))
                .andExpect(jsonPath("$.offeringName").value("Baño completo"))
                .andExpect(jsonPath("$.providerName").value("Vet Sol"))
                .andExpect(jsonPath("$.date").value("2026-11-11"))
                .andExpect(jsonPath("$.price").value(45000.00))
                .andExpect(jsonPath("$.status").value("PENDIENTE"));
    }

    @Test
    void rejectsMissingFields() throws Exception {
        mockMvc.perform(post("/service-requests").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(serviceRequestRegistry, never()).createServiceRequest(any());
    }

    @Test
    void rejectsUnknownPurchaseType() throws Exception {
        postRequest("\"REGALO\"").andExpect(status().isBadRequest());
        verify(serviceRequestRegistry, never()).createServiceRequest(any());
    }

    @Test
    void returnsBadRequestWhenPurchaseTypeDoesNotApply() throws Exception {
        when(serviceRequestRegistry.createServiceRequest(any()))
                .thenThrow(new IllegalArgumentException("Purchase type only applies to products"));

        postRequest("\"RETIRO\"").andExpect(status().isBadRequest());
    }

    @Test
    void returnsForbiddenForPetOfAnotherOwner() throws Exception {
        when(serviceRequestRegistry.createServiceRequest(any())).thenThrow(new PetNotOwnedException());

        postRequest("null").andExpect(status().isForbidden());
    }

    @Test
    void returnsNotFoundForUnknownPet() throws Exception {
        when(serviceRequestRegistry.createServiceRequest(any())).thenThrow(new PetNotFoundException(ID));

        postRequest("null").andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForUnknownSlot() throws Exception {
        when(serviceRequestRegistry.createServiceRequest(any())).thenThrow(new TimeSlotNotFoundException(ID));

        postRequest("null").andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictForUnavailableSlot() throws Exception {
        when(serviceRequestRegistry.createServiceRequest(any())).thenThrow(new TimeSlotUnavailableException("occupied"));

        postRequest("null").andExpect(status().isConflict());
    }

    @Test
    void returnsConflictForDuplicateRequest() throws Exception {
        when(serviceRequestRegistry.createServiceRequest(any())).thenThrow(new DuplicateServiceRequestException());

        postRequest("null").andExpect(status().isConflict());
    }
}
