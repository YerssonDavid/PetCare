package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.CreateTimeSlotRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.dto.TimeSlotResponse;
import com.petcare.petcarebackend.application.port.in.query.TimeSlotQuery;
import com.petcare.petcarebackend.application.port.in.registry.TimeSlotRegistry;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services/{offeringId}/slots")
@RequiredArgsConstructor
@Tag(name = "Franjas horarias", description = "Creación y consulta de franjas horarias de servicios y productos")
public class TimeSlotController {

    private final TimeSlotRegistry timeSlotRegistry;
    private final TimeSlotQuery timeSlotQuery;

    @PostMapping
    @Operation(
            summary = "Crear franja horaria",
            description = "Crea una franja para un servicio o producto del proveedor: la cita de un servicio, o la hora de retiro o entrega de un producto. "
                    + "Debe terminar a más tardar a las 17:00 y empezar en el futuro (hora de Bogotá). "
                    + "Las franjas de servicios no pueden cruzarse entre sí; las de productos no se validan. "
                    + "Temporalmente no requiere autenticación hasta implementar JWT."
    )
    @ApiResponse(responseCode = "201", description = "Franja creada; devuelve el id generado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol PROVIDER o el servicio es de otro proveedor",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Servicio o proveedor inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "La franja del servicio se cruza con otra franja de servicio del proveedor",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<RegisterResponse> createTimeSlot(@PathVariable UUID offeringId,
                                                           @Valid @RequestBody CreateTimeSlotRequest request) {
        RegisterResponse response = timeSlotRegistry.createTimeSlot(offeringId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/free")
    @Operation(
            summary = "Consultar franjas libres",
            description = "Lista las franjas libres y futuras de un servicio o producto, ordenadas por fecha y hora."
    )
    @ApiResponse(responseCode = "200", description = "Franjas libres; lista vacía si no hay ninguna",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TimeSlotResponse.class))))
    @ApiResponse(responseCode = "404", description = "Servicio inexistente",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Error interno",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<List<TimeSlotResponse>> listFreeTimeSlots(@PathVariable UUID offeringId) {
        return ResponseEntity.ok(timeSlotQuery.listFreeTimeSlots(offeringId));
    }
}
