package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.PetResponse;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.port.in.query.PetQuery;
import com.petcare.petcarebackend.domain.enums.TypePet;
import com.petcare.petcarebackend.infrastructure.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    private static final UUID OWNER_ID = UUID.randomUUID();

    @Mock
    private PetQuery petQuery;

    @InjectMocks
    private PetController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalException())
                .build();
    }

    @Test
    void returnsThePetsOfTheOwner() throws Exception {
        TypePet type = TypePet.values()[0];
        UUID petId = UUID.randomUUID();
        when(petQuery.listPetsByOwner(OWNER_ID))
                .thenReturn(List.of(new PetResponse(petId, "Luna", type, 3, "Tranquila")));

        mockMvc.perform(get("/pets").principal(OWNER_ID::toString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(petId.toString()))
                .andExpect(jsonPath("$[0].name").value("Luna"))
                .andExpect(jsonPath("$[0].typePet").value(type.name()))
                .andExpect(jsonPath("$[0].age").value(3))
                .andExpect(jsonPath("$[0].description").value("Tranquila"));
    }

    @Test
    void returnsNotFoundForUnknownOwner() throws Exception {
        when(petQuery.listPetsByOwner(OWNER_ID)).thenThrow(new OwnerNotFoundException(OWNER_ID));

        mockMvc.perform(get("/pets").principal(OWNER_ID::toString))
                .andExpect(status().isNotFound());
    }
}
