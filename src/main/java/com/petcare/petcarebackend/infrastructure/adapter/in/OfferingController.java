package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.OfferingDetailResponse;
import com.petcare.petcarebackend.application.dto.OfferingResponse;
import com.petcare.petcarebackend.application.dto.PublishOfferingRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.port.in.query.OfferingDetailQuery;
import com.petcare.petcarebackend.application.port.in.query.OfferingQuery;
import com.petcare.petcarebackend.application.port.in.registry.OfferingRegistry;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "Publicación y búsqueda de servicios y productos")
public class OfferingController {

    private final OfferingRegistry offeringRegistry;
    private final OfferingQuery offeringQuery;
    private final OfferingDetailQuery offeringDetailQuery;

    @PostMapping
    @Operation(
            summary = "Publicar servicio o producto",
            description = "Publica un servicio o producto del proveedor autenticado; requiere el rol PROVIDER."
    )
    @ApiResponse(responseCode = "201", description = "Publicado; devuelve el id generado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol PROVIDER",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Proveedor inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "El proveedor ya tiene un servicio o producto con ese nombre",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<RegisterResponse> publishOffering(@Valid @RequestBody PublishOfferingRequest request, Principal principal) {
        RegisterResponse response = offeringRegistry.publishOffering(CurrentAccount.idOf(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Buscar servicios y productos",
            description = "Lista los servicios y productos de una categoría; sin categoría, los lista todos."
    )
    @ApiResponse(responseCode = "200", description = "Resultados de la búsqueda; lista vacía si no hay ninguno",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OfferingResponse.class))))
    @ApiResponse(responseCode = "400", description = "Categoría inválida",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<List<OfferingResponse>> listOfferings(@RequestParam(required = false) OfferingCategory category) {
        return ResponseEntity.ok(offeringQuery.listOfferings(category));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar detalle",
            description = "Devuelve la descripción, condiciones, precio, franjas libres y teléfono del proveedor de un servicio o producto."
    )
    @ApiResponse(responseCode = "200", description = "Detalle del servicio o producto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferingDetailResponse.class)))
    @ApiResponse(responseCode = "400", description = "Identificador con formato inválido",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Servicio o producto inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<OfferingDetailResponse> getOfferingDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(offeringDetailQuery.getOfferingDetail(id));
    }
}
