package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.AcceptServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.CreateServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;
import com.petcare.petcarebackend.application.dto.RejectServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.port.in.query.ReceivedServiceRequestQuery;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestAnswerRegistry;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestRegistry;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/service-requests")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Solicitudes de servicios y productos")
public class ServiceRequestController {

    private final ServiceRequestRegistry serviceRequestRegistry;
    private final ServiceRequestAnswerRegistry serviceRequestAnswerRegistry;
    private final ReceivedServiceRequestQuery receivedServiceRequestQuery;

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
    @ApiResponse(responseCode = "409", description = "La franja está ocupada o ya empezó, la mascota ya tiene un servicio a esa hora, o ya solicitó ese producto en esa franja",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(@Valid @RequestBody CreateServiceRequestRequest request) {
        ServiceRequestResponse response = serviceRequestRegistry.createServiceRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/received")
    @Operation(
            summary = "Listar solicitudes recibidas",
            description = "Devuelve las solicitudes de un proveedor, de la más reciente a la más antigua, con los datos de la mascota y el correo del cliente. "
                    + "Se pueden filtrar por estado. Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "200", description = "Solicitudes del proveedor; lista vacía si no tiene ninguna",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReceivedServiceRequestResponse.class))))
    @ApiResponse(responseCode = "400", description = "Falta providerId o el estado no es válido",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol PROVIDER",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Proveedor inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    // TODO: remove providerId once JWT exists; the provider ID must come from the token
    public List<ReceivedServiceRequestResponse> listReceivedServiceRequests(@RequestParam UUID providerId,
                                                                            @RequestParam(required = false) RequestStatus status) {
        return receivedServiceRequestQuery.listReceivedServiceRequests(providerId, status);
    }

    @PatchMapping("/{id}/accept")
    @Operation(
            summary = "Aceptar solicitud",
            description = "Cambia a ACEPTADO una solicitud PENDIENTE del proveedor, siempre que la franja no haya empezado. "
                    + "Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "200", description = "Solicitud aceptada; devuelve su confirmación",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceRequestResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol PROVIDER o la solicitud es de otro proveedor",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Proveedor o solicitud inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "La solicitud ya fue respondida o la franja ya empezó",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ServiceRequestResponse acceptServiceRequest(@PathVariable UUID id,
                                                       @Valid @RequestBody AcceptServiceRequestRequest request) {
        return serviceRequestAnswerRegistry.acceptServiceRequest(id, request);
    }

    @PatchMapping("/{id}/reject")
    @Operation(
            summary = "Rechazar solicitud",
            description = "Cambia a RECHAZADO una solicitud PENDIENTE del proveedor, con un motivo obligatorio. "
                    + "Si es de un servicio y la franja no ha empezado, vuelve a quedar libre. "
                    + "Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "200", description = "Solicitud rechazada; devuelve su confirmación con el motivo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceRequestResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o motivo vacío",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol PROVIDER o la solicitud es de otro proveedor",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Proveedor o solicitud inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "La solicitud ya fue respondida",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ServiceRequestResponse rejectServiceRequest(@PathVariable UUID id,
                                                       @Valid @RequestBody RejectServiceRequestRequest request) {
        return serviceRequestAnswerRegistry.rejectServiceRequest(id, request);
    }
}
