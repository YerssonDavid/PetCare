package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.OfferingResponse;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OfferingAlreadyExistsException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.port.in.query.OfferingQuery;
import com.petcare.petcarebackend.application.port.in.registry.OfferingRegistry;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.infrastructure.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OfferingControllerTest {

    private static final UUID PROVIDER_ID = UUID.randomUUID();

    @Mock
    private OfferingRegistry offeringRegistry;

    @Mock
    private OfferingQuery offeringQuery;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OfferingController(offeringRegistry, offeringQuery))
                .setControllerAdvice(new GlobalException())
                .build();
    }

    private String body(String price) {
        return """
                {
                  "providerId": "%s",
                  "name": "Baño completo",
                  "description": "Baño y secado",
                  "conditions": "Vacunas al día",
                  "price": %s,
                  "category": "PELUQUERIA",
                  "type": "SERVICIO"
                }
                """.formatted(PROVIDER_ID, price);
    }

    @Test
    void publishesOffering() throws Exception {
        UUID id = UUID.randomUUID();
        when(offeringRegistry.publishOffering(any()))
                .thenReturn(new RegisterResponse(id, "ok", LocalDateTime.now()));

        mockMvc.perform(post("/services").contentType(MediaType.APPLICATION_JSON).content(body("45000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void rejectsNegativePrice() throws Exception {
        mockMvc.perform(post("/services").contentType(MediaType.APPLICATION_JSON).content(body("-1")))
                .andExpect(status().isBadRequest());
        verify(offeringRegistry, never()).publishOffering(any());
    }

    @Test
    void rejectsPriceWithMoreThanTwoDecimals() throws Exception {
        mockMvc.perform(post("/services").contentType(MediaType.APPLICATION_JSON).content(body("10.005")))
                .andExpect(status().isBadRequest());
        verify(offeringRegistry, never()).publishOffering(any());
    }

    @Test
    void returnsNotFoundForUnknownProvider() throws Exception {
        when(offeringRegistry.publishOffering(any())).thenThrow(new ProviderNotFoundException(PROVIDER_ID));

        mockMvc.perform(post("/services").contentType(MediaType.APPLICATION_JSON).content(body("45000")))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictForRepeatedName() throws Exception {
        when(offeringRegistry.publishOffering(any())).thenThrow(new OfferingAlreadyExistsException("Baño completo"));

        mockMvc.perform(post("/services").contentType(MediaType.APPLICATION_JSON).content(body("45000")))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsForbiddenWithoutProviderRole() throws Exception {
        when(offeringRegistry.publishOffering(any())).thenThrow(new RoleNotAllowedException("denied"));

        mockMvc.perform(post("/services").contentType(MediaType.APPLICATION_JSON).content(body("45000")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsOfferingsByCategory() throws Exception {
        UUID id = UUID.randomUUID();
        when(offeringQuery.listOfferings(OfferingCategory.PASEO)).thenReturn(List.of(
                new OfferingResponse(id, PROVIDER_ID, "Paseo", "Una hora", null,
                        new BigDecimal("20000.00"), OfferingCategory.PASEO, OfferingType.SERVICIO, null)));

        mockMvc.perform(get("/services").param("category", "PASEO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].providerId").value(PROVIDER_ID.toString()))
                .andExpect(jsonPath("$[0].category").value("PASEO"))
                .andExpect(jsonPath("$[0].type").value("SERVICIO"));
    }

    @Test
    void listsEverythingWithoutCategory() throws Exception {
        when(offeringQuery.listOfferings(null)).thenReturn(List.of());

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rejectsInvalidCategory() throws Exception {
        mockMvc.perform(get("/services").param("category", "NOPE"))
                .andExpect(status().isBadRequest());
    }
}
