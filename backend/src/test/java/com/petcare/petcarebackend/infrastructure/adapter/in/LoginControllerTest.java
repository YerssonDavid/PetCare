package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.LoginRequest;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.dto.RefreshTokenRequest;
import com.petcare.petcarebackend.application.exception.InvalidCredentialsException;
import com.petcare.petcarebackend.application.exception.InvalidRefreshTokenException;
import com.petcare.petcarebackend.application.port.in.login.LoginUser;
import com.petcare.petcarebackend.application.port.in.login.TokenRefresher;
import com.petcare.petcarebackend.infrastructure.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Standalone MockMvc: it covers the JSON contract and the error mapping, not the rules in SecurityConfig
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private static final String URL = "/auth/login";
    private static final String REFRESH_URL = "/auth/refresh";
    private static final String EMAIL = "ana@mail.com";
    private static final String PASSWORD = "secret123";
    private static final String VALID_BODY = """
            {"email":"ana@mail.com","password":"secret123"}
            """;

    @Mock
    private LoginUser loginUser;

    @Mock
    private TokenRefresher tokenRefresher;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LoginController(loginUser, tokenRefresher))
                .setControllerAdvice(new GlobalException())
                .build();
    }

    private ResultActions login(String body) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void returnsTheTokenAsJson() throws Exception {
        when(loginUser.loginUsers(new LoginRequest(EMAIL, PASSWORD))).thenReturn(new LoginResponse("signed-token", "refresh-token"));

        login(VALID_BODY)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("signed-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "email    | {\"password\":\"secret123\"}",
            "email    | {\"email\":null,\"password\":\"secret123\"}",
            "email    | {\"email\":\"\",\"password\":\"secret123\"}",
            "email    | {\"email\":\"   \",\"password\":\"secret123\"}",
            "email    | {\"email\":\"not-an-email\",\"password\":\"secret123\"}",
            "password | {\"email\":\"ana@mail.com\"}",
            "password | {\"email\":\"ana@mail.com\",\"password\":null}",
            "password | {\"email\":\"ana@mail.com\",\"password\":\"\"}",
            "password | {\"email\":\"ana@mail.com\",\"password\":\"   \"}"
    }, quoteCharacter = '`')
    void rejectsAnInvalidFieldBeforeReachingTheUseCase(String field, String body) throws Exception {
        login(body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors." + field).exists());
        verify(loginUser, never()).loginUsers(any());
    }

    @Test
    void reportsBothFieldsWhenTheBodyIsEmpty() throws Exception {
        login("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
        verify(loginUser, never()).loginUsers(any());
    }

    @Test
    void rejectsBrokenJson() throws Exception {
        login("{oops")
                .andExpect(status().isBadRequest());
        verify(loginUser, never()).loginUsers(any());
    }

    // A short password must answer like any wrong password, so login does not repeat the registration size rule
    @Test
    void doesNotApplyThePasswordSizeRuleOfTheRegistration() throws Exception {
        when(loginUser.loginUsers(new LoginRequest(EMAIL, "abc"))).thenThrow(new InvalidCredentialsException());

        login("""
                {"email":"ana@mail.com","password":"abc"}
                """)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void answersUnauthorizedWithAProblemDetailWhenTheCredentialsAreInvalid() throws Exception {
        when(loginUser.loginUsers(any())).thenThrow(new InvalidCredentialsException());

        login(VALID_BODY)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos"));
    }

    // "a@b" passes @Email, but the Email value object requires a domain extension
    @Test
    void answersBadRequestWhenTheDomainRejectsTheEmail() throws Exception {
        when(loginUser.loginUsers(any())).thenThrow(new IllegalArgumentException("El formato del correo no es válido"));

        login("""
                {"email":"a@b","password":"secret123"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El formato del correo no es válido"));
    }

    private ResultActions refresh(String body) throws Exception {
        return mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void refreshReturnsANewPairOfTokens() throws Exception {
        when(tokenRefresher.refreshToken(new RefreshTokenRequest("old-refresh")))
                .thenReturn(new LoginResponse("new-access", "new-refresh"));

        refresh("""
                {"refreshToken":"old-refresh"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void refreshRequiresTheToken() throws Exception {
        refresh("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.refreshToken").value("Este campo es obligatorio"));
        verify(tokenRefresher, never()).refreshToken(any());
    }

    @Test
    void refreshAnswersUnauthorizedForAnInvalidToken() throws Exception {
        when(tokenRefresher.refreshToken(any())).thenThrow(new InvalidRefreshTokenException());

        refresh("""
                {"refreshToken":"stolen"}
                """)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Sesión inválida"));
    }
}
