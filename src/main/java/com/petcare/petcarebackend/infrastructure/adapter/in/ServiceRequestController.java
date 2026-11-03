package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.CreateServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestRegistry;
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
@RequestMapping("/service-requests")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Solicitudes de servicios y productos")
public class ServiceRequestController {

    private final ServiceRequestRegistry serviceRequestRegistry;

    @PostMapping
    @Operation(
            summary = "Solicitar servicio o producto",
            description = "Crea una solicitud en estado PENDIENTE para una mascota del propietario. "
                    + "Un servicio ocupa la franja de inmediato. Un producto no la ocupa y requiere purchaseType (RETIRO o ENTREGA). "
                    + "Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "201", description = "Solicitud creada; devuelve su confirmación",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceRequestResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o purchaseType que no corresponde al tipo",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol USER o la mascota es de otro propietario",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Propietario, mascota o franja inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "La franja está ocupada o ya empezó, o la mascota ya tiene un servicio a esa hora",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(@Valid @RequestBody CreateServiceRequestRequest request) {
        ServiceRequestResponse response = serviceRequestRegistry.createServiceRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
