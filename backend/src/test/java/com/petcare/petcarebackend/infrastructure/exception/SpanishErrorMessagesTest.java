package com.petcare.petcarebackend.infrastructure.exception;

import com.petcare.petcarebackend.application.port.in.login.LoginUser;
import com.petcare.petcarebackend.application.port.in.login.TokenRefresher;
import com.petcare.petcarebackend.application.port.in.query.OfferingDetailQuery;
import com.petcare.petcarebackend.application.port.in.query.OfferingQuery;
import com.petcare.petcarebackend.application.port.in.registry.OfferingRegistry;
import com.petcare.petcarebackend.application.port.in.registry.OwnerRegistry;
import com.petcare.petcarebackend.application.port.in.registry.PetRegistry;
import com.petcare.petcarebackend.application.port.in.registry.ProviderRegistry;
import com.petcare.petcarebackend.infrastructure.adapter.in.LoginController;
import com.petcare.petcarebackend.infrastructure.adapter.in.OfferingController;
import com.petcare.petcarebackend.infrastructure.adapter.in.RegistryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Every error a client can read comes in Spanish, including the ones Spring and Bean Validation produce
@ExtendWith(MockitoExtension.class)
class SpanishErrorMessagesTest {

    private static final String PROVIDER_ID = UUID.randomUUID().toString();

    @Mock
    private LoginUser loginUser;
    @Mock
    private TokenRefresher tokenRefresher;
    @Mock
    private OfferingRegistry offeringRegistry;
    @Mock
    private OfferingQuery offeringQuery;
    @Mock
    private OfferingDetailQuery offeringDetailQuery;
    @Mock
    private OwnerRegistry ownerRegistry;
    @Mock
    private PetRegistry petRegistry;
    @Mock
    private ProviderRegistry providerRegistry;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new LoginController(loginUser, tokenRefresher),
                        new OfferingController(offeringRegistry, offeringQuery, offeringDetailQuery),
                        new RegistryController(ownerRegistry, petRegistry, providerRegistry))
                .setControllerAdvice(new GlobalException())
                .build();
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void requiredField() throws Exception {
        postJson("/auth/login", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.detail").value("Uno o más campos no son válidos"))
                .andExpect(jsonPath("$.errors.email").value("Este campo es obligatorio"))
                .andExpect(jsonPath("$.errors.password").value("Este campo es obligatorio"));
    }

    @Test
    void requiredObject() throws Exception {
        postJson("/services", """
                {"name":"Baño","description":"Baño y secado","price":10}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.category").value("Este campo es obligatorio"));
    }

    @Test
    void emailFormat() throws Exception {
        postJson("/auth/login", """
                {"email":"no-es-correo","password":"secret123"}
                """)
                .andExpect(jsonPath("$.errors.email").value("El correo no tiene un formato válido"));
    }

    @Test
    void sizeAndPhonePattern() throws Exception {
        postJson("/registry/owner", """
                {"name":"Ana","email":"ana@mail.com","phone":"abc","password":"corta"}
                """)
                .andExpect(jsonPath("$.errors.password").value("Debe tener entre 8 y 72 caracteres"))
                .andExpect(jsonPath("$.errors.phone").value("El formato no es válido"));
    }

    @Test
    void negativeNumberAndDigits() throws Exception {
        postJson("/services", """
                {"name":"Baño","description":"Baño y secado","price":-1,"category":"PASEO","type":"SERVICIO"}
                """)
                .andExpect(jsonPath("$.errors.price").value("Debe ser mayor o igual a 0"));
        postJson("/services", """
                {"name":"Baño","description":"Baño y secado","price":10.005,"category":"PASEO","type":"SERVICIO"}
                """)
                .andExpect(jsonPath("$.errors.price").value("Debe tener como máximo 8 dígitos enteros y 2 decimales"));
    }

    @Test
    void maximumValue() throws Exception {
        postJson("/registry/provider", """
                {"nameCommercial":"Vet","typeProvider":"CLINICA","email":"vet@mail.com","phone":"3111111111",
                 "password":"secret123","city":"Bogotá","experienceYears":81}
                """)
                .andExpect(jsonPath("$.errors.experienceYears").value("Debe ser menor o igual a 80"));
    }

    @Test
    void malformedJson() throws Exception {
        postJson("/auth/login", "{oops")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.detail").value("El cuerpo de la solicitud está vacío o tiene un formato o un valor inválido"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void wrongEnumValueInTheBody() throws Exception {
        postJson("/services", """
                {"name":"Baño","description":"Baño y secado","price":10,"category":"NOPE","type":"SERVICIO"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El cuerpo de la solicitud está vacío o tiene un formato o un valor inválido"));
    }

    @Test
    void wrongQueryParameter() throws Exception {
        mockMvc.perform(get("/services").param("category", "NOPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.detail").value("El parámetro 'category' no tiene un valor válido"));
    }

    @Test
    void wrongPathVariable() throws Exception {
        mockMvc.perform(get("/services/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El parámetro 'id' no tiene un valor válido"));
    }

    @Test
    void methodNotAllowed() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Método no permitido"))
                .andExpect(jsonPath("$.detail").value("El método GET no está permitido en esta ruta"));
    }

    @Test
    void unsupportedContentType() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.title").value("Formato no compatible"))
                .andExpect(jsonPath("$.detail").value("El tipo de contenido de la solicitud no es compatible"));
    }

    @Test
    void unexpectedErrorRevealsNothing() throws Exception {
        when(loginUser.loginUsers(any())).thenThrow(new IllegalStateException("connection refused at 10.0.0.5"));

        postJson("/auth/login", """
                {"email":"ana@mail.com","password":"secret123"}
                """)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Error interno"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado"));
    }
}
