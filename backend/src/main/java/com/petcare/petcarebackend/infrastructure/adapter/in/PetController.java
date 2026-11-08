package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.PetResponse;
import com.petcare.petcarebackend.application.port.in.query.PetQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Mascotas", description = "Consulta de mascotas")
public class PetController {

    private final PetQuery petQuery;

    @GetMapping
    @Operation(
            summary = "Listar mascotas",
            description = "Devuelve las mascotas de un propietario. Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "200", description = "Mascotas del propietario; lista vacía si no tiene ninguna",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PetResponse.class))))
    @ApiResponse(responseCode = "400", description = "ownerId ausente o con formato inválido",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Propietario inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    // TODO: remove ownerId once JWT exists; the owner ID must come from the token
    public ResponseEntity<List<PetResponse>> getPets(@RequestParam UUID ownerId) {
        return ResponseEntity.ok(petQuery.listPetsByOwner(ownerId));
    }
}
