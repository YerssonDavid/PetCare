package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.PetResponse;
import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.DuplicateServiceRequestException;
import com.petcare.petcarebackend.application.exception.NotRequestProviderException;
import com.petcare.petcarebackend.application.exception.PetNotFoundException;
import com.petcare.petcarebackend.application.exception.PetNotOwnedException;
import com.petcare.petcarebackend.application.exception.ServiceRequestNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotUnavailableException;
import com.petcare.petcarebackend.application.port.in.query.ReceivedServiceRequestQuery;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestAnswerRegistry;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestRegistry;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.domain.enums.TypePet;
import com.petcare.petcarebackend.domain.exception.RequestNotPendingException;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceRequestControllerTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();

    @Mock
    private ServiceRequestRegistry serviceRequestRegistry;

    @Mock
    private ServiceRequestAnswerRegistry serviceRequestAnswerRegistry;

    @Mock
    private ReceivedServiceRequestQuery receivedServiceRequestQuery;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceRequestController(
                        serviceRequestRegistry, serviceRequestAnswerRegistry, receivedServiceRequestQuery))
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

    private ServiceRequestResponse answered(RequestStatus requestStatus, String reason) {
        return new ServiceRequestResponse(
                ID, "Firulais", "Baño completo", OfferingType.SERVICIO, "Vet Sol",
                LocalDate.of(2026, 11, 11), LocalTime.of(10, 0), LocalTime.of(11, 0),
                new BigDecimal("45000.00"), requestStatus, null, reason, LocalDateTime.of(2026, 11, 10, 8, 0));
    }

    private ResultActions patchAccept() throws Exception {
        return mockMvc.perform(patch("/service-requests/{id}/accept", ID).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "providerId": "%s" }
                        """.formatted(PROVIDER_ID)));
    }

    private ResultActions patchReject(String reason) throws Exception {
        return mockMvc.perform(patch("/service-requests/{id}/reject", ID).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "providerId": "%s", "reason": %s }
                        """.formatted(PROVIDER_ID, reason)));
    }

    @Test
    void acceptsRequestAndReturnsConfirmation() throws Exception {
        when(serviceRequestAnswerRegistry.acceptServiceRequest(eq(ID), any()))
                .thenReturn(answered(RequestStatus.ACEPTADO, null));

        patchAccept()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.status").value("ACEPTADO"));
    }

    @Test
    void acceptRequiresProviderId() throws Exception {
        mockMvc.perform(patch("/service-requests/{id}/accept", ID).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(serviceRequestAnswerRegistry, never()).acceptServiceRequest(any(), any());
    }

    @Test
    void rejectsRequestAndReturnsTheReason() throws Exception {
        when(serviceRequestAnswerRegistry.rejectServiceRequest(eq(ID), any()))
                .thenReturn(answered(RequestStatus.RECHAZADO, "Sin disponibilidad"));

        patchReject("\"Sin disponibilidad\"")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECHAZADO"))
                .andExpect(jsonPath("$.rejectionReason").value("Sin disponibilidad"));
    }

    @Test
    void rejectRequiresAReason() throws Exception {
        patchReject("null").andExpect(status().isBadRequest());
        patchReject("\"   \"").andExpect(status().isBadRequest());
        verify(serviceRequestAnswerRegistry, never()).rejectServiceRequest(any(), any());
    }

    @Test
    void rejectLimitsTheLengthOfTheReason() throws Exception {
        patchReject("\"" + "a".repeat(501) + "\"").andExpect(status().isBadRequest());
        verify(serviceRequestAnswerRegistry, never()).rejectServiceRequest(any(), any());
    }

    @Test
    void returnsConflictWhenAlreadyAnswered() throws Exception {
        when(serviceRequestAnswerRegistry.acceptServiceRequest(eq(ID), any())).thenThrow(new RequestNotPendingException());

        patchAccept().andExpect(status().isConflict());
    }

    @Test
    void returnsConflictWhenAcceptingAfterTheSlotStarted() throws Exception {
        when(serviceRequestAnswerRegistry.acceptServiceRequest(eq(ID), any()))
                .thenThrow(new TimeSlotUnavailableException("started"));

        patchAccept().andExpect(status().isConflict());
    }

    @Test
    void returnsForbiddenForRequestOfAnotherProvider() throws Exception {
        when(serviceRequestAnswerRegistry.rejectServiceRequest(eq(ID), any())).thenThrow(new NotRequestProviderException());

        patchReject("\"motivo\"").andExpect(status().isForbidden());
    }

    @Test
    void returnsNotFoundForUnknownRequest() throws Exception {
        when(serviceRequestAnswerRegistry.acceptServiceRequest(eq(ID), any())).thenThrow(new ServiceRequestNotFoundException(ID));

        patchAccept().andExpect(status().isNotFound());
    }

    @Test
    void listsReceivedRequestsWithPetDataAndClientEmail() throws Exception {
        when(receivedServiceRequestQuery.listReceivedServiceRequests(PROVIDER_ID, RequestStatus.PENDIENTE))
                .thenReturn(List.of(new ReceivedServiceRequestResponse(
                        ID, new PetResponse(UUID.randomUUID(), "Firulais", TypePet.PERRO, 3, "Juguetón"), "ana@mail.com",
                        "Baño completo", OfferingType.SERVICIO, LocalDate.of(2026, 11, 11), LocalTime.of(10, 0), LocalTime.of(11, 0),
                        new BigDecimal("45000.00"), RequestStatus.PENDIENTE, null, null, LocalDateTime.of(2026, 11, 10, 8, 0))));

        mockMvc.perform(get("/service-requests/received")
                        .param("providerId", PROVIDER_ID.toString())
                        .param("status", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID.toString()))
                .andExpect(jsonPath("$[0].pet.name").value("Firulais"))
                .andExpect(jsonPath("$[0].pet.typePet").value("PERRO"))
                .andExpect(jsonPath("$[0].ownerEmail").value("ana@mail.com"))
                .andExpect(jsonPath("$[0].status").value("PENDIENTE"));
    }

    @Test
    void statusFilterIsOptional() throws Exception {
        when(receivedServiceRequestQuery.listReceivedServiceRequests(PROVIDER_ID, null)).thenReturn(List.of());

        mockMvc.perform(get("/service-requests/received").param("providerId", PROVIDER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rejectsUnknownStatusFilter() throws Exception {
        mockMvc.perform(get("/service-requests/received")
                        .param("providerId", PROVIDER_ID.toString())
                        .param("status", "CANCELADO"))
                .andExpect(status().isBadRequest());
        verify(receivedServiceRequestQuery, never()).listReceivedServiceRequests(any(), any());
    }

    @Test
    void receivedRequiresProviderId() throws Exception {
        mockMvc.perform(get("/service-requests/received")).andExpect(status().isBadRequest());
    }
}
