package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.LoginRequest;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.dto.RefreshTokenRequest;
import com.petcare.petcarebackend.application.port.in.login.LoginUser;
import com.petcare.petcarebackend.application.port.in.login.TokenRefresher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio de sesión y renovación de tokens de propietarios y proveedores")
public class LoginController {

    private final LoginUser loginUser;
    private final TokenRefresher tokenRefresher;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida el correo y la contraseña de un propietario o proveedor. Devuelve un token de acceso (JWT) válido por 1 hora, con el ID y el rol de la cuenta, y un token de actualización válido por 7 días."
    )
    @ApiResponse(responseCode = "200", description = "Credenciales válidas; devuelve los dos tokens",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos ausentes o con formato inválido",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos, o cuenta inactiva",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<LoginResponse> loginUsers(@Valid @RequestBody LoginRequest login) {
        return ResponseEntity.ok(loginUser.loginUsers(login));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(
            summary = "Renovar tokens",
            description = "Cambia un token de actualización por un nuevo par de tokens. Cada token de actualización sirve una sola vez: el cliente debe guardar el nuevo. "
                    + "Si se usa de nuevo uno ya usado, se cierran todas las sesiones de la cuenta."
    )
    @ApiResponse(responseCode = "200", description = "Token válido; devuelve un nuevo par de tokens",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Falta el token de actualización",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token de actualización inválido, expirado o ya usado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(tokenRefresher.refreshToken(request));
    }
}
