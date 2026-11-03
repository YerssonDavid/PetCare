package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.OfferingResponse;
import com.petcare.petcarebackend.application.dto.PublishOfferingRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "Publicación y búsqueda de servicios y productos")
public class OfferingController {

    private final OfferingRegistry offeringRegistry;
    private final OfferingQuery offeringQuery;

    @PostMapping
    @Operation(
            summary = "Publicar servicio o producto",
            description = "Publica un servicio o producto de un proveedor; requiere el rol PROVIDER. Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "201", description = "Publicado; devuelve el id generado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol PROVIDER",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Proveedor inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "El proveedor ya tiene un servicio o producto con ese nombre",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<RegisterResponse> publishOffering(@Valid @RequestBody PublishOfferingRequest request) {
        RegisterResponse response = offeringRegistry.publishOffering(request);
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
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<List<OfferingResponse>> listOfferings(@RequestParam(required = false) OfferingCategory category) {
        return ResponseEntity.ok(offeringQuery.listOfferings(category));
    }
}
