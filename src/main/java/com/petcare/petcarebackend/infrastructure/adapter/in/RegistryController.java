package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.RegisterOwnerRequest;
import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.ResponseHttp;
import com.petcare.petcarebackend.application.port.in.OwnerRegistry;
import com.petcare.petcarebackend.application.port.in.PetRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registry")
@RequiredArgsConstructor
@Tag(name = "Registro", description = "Registro de nuevas cuentas")
public class RegistryController {

    private final OwnerRegistry ownerRegistry;
    private final PetRegistry petRegistry;

    @PostMapping("/owner")
    @Operation(
            summary = "Registrar propietario",
            description = "Crea una cuenta de propietario de mascotas con rol USER. No requiere autenticación."
    )
    @ApiResponse(responseCode = "201", description = "Propietario registrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseHttp.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "No se pudo completar el registro (p. ej. email ya registrado)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseHttp.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ResponseHttp> registryOwner(@Valid @RequestBody RegisterOwnerRequest register) {
        ResponseHttp response = ownerRegistry.registerOwner(register);
        HttpStatus status = response.success() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/pet")
    @Operation(
            summary = "Registrar mascota",
            description = "Registra una mascota para un propietario existente. Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "201", description = "Mascota registrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseHttp.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "No se pudo completar el registro (p. ej. propietario inexistente o mascota ya registrada)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseHttp.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ResponseHttp> registryPet(@Valid @RequestBody RegisterPetRequest register) {
        ResponseHttp response = petRegistry.registerPet(register);
        HttpStatus status = response.success() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(response);
    }
}
