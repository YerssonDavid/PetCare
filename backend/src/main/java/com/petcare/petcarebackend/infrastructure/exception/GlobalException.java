package com.petcare.petcarebackend.infrastructure.exception;

import com.petcare.petcarebackend.application.exception.DuplicateServiceRequestException;
import com.petcare.petcarebackend.application.exception.InvalidCredentialsException;
import com.petcare.petcarebackend.application.exception.InvalidRefreshTokenException;
import com.petcare.petcarebackend.application.exception.NotOfferingOwnerException;
import com.petcare.petcarebackend.application.exception.NotRequestParticipantException;
import com.petcare.petcarebackend.application.exception.NotRequestProviderException;
import com.petcare.petcarebackend.application.exception.OfferingAlreadyExistsException;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.exception.OwnerAlreadyExistsException;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.PetAlreadyExistsException;
import com.petcare.petcarebackend.application.exception.PetNotFoundException;
import com.petcare.petcarebackend.application.exception.PetNotOwnedException;
import com.petcare.petcarebackend.application.exception.ProviderAlreadyExistsException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.ServiceRequestNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotOverlapException;
import com.petcare.petcarebackend.application.exception.TimeSlotUnavailableException;
import com.petcare.petcarebackend.domain.exception.RequestNotPendingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalException extends ResponseEntityExceptionHandler {

    // Bean Validation on request DTOs (@Valid)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no son válidos");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    // Spring's own errors (malformed JSON, wrong parameter type, unknown route...) come with English text
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        if (status.is5xxServerError()) {
            log.error("Framework error", ex);
        }
        ProblemDetail problem = problem(status, titleOf(status), detailOf(ex, status));
        return super.handleExceptionInternal(ex, problem, headers, statusCode, request);
    }

    private String titleOf(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Datos inválidos";
            case NOT_FOUND -> "Recurso no encontrado";
            case METHOD_NOT_ALLOWED -> "Método no permitido";
            case NOT_ACCEPTABLE, UNSUPPORTED_MEDIA_TYPE -> "Formato no compatible";
            default -> status.is5xxServerError() ? "Error interno" : "Solicitud no válida";
        };
    }

    private String detailOf(Exception ex, HttpStatus status) {
        return switch (ex) {
            case HttpMessageNotReadableException e -> "El cuerpo de la solicitud está vacío o tiene un formato o un valor inválido";
            case MethodArgumentTypeMismatchException e -> "El parámetro '" + e.getName() + "' no tiene un valor válido";
            case MissingServletRequestParameterException e -> "Falta el parámetro obligatorio '" + e.getParameterName() + "'";
            case HttpRequestMethodNotSupportedException e -> "El método " + e.getMethod() + " no está permitido en esta ruta";
            case HttpMediaTypeNotSupportedException e -> "El tipo de contenido de la solicitud no es compatible";
            case HttpMediaTypeNotAcceptableException e -> "No se puede responder en el formato solicitado";
            case NoResourceFoundException e -> "La ruta solicitada no existe";
            default -> status.is5xxServerError() ? "Ocurrió un error inesperado" : "La solicitud no es válida";
        };
    }

    // No token, or a token the decoder rejected; handed over by SecurityErrorHandler
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        String detail = ex instanceof OAuth2AuthenticationException
                ? "El token de acceso no es válido o ya expiró"
                : "Debes iniciar sesión para acceder a este recurso";
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado", detail);
    }

    // Valid token without the role the route requires; handed over by SecurityErrorHandler
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", "No tienes permiso para realizar esta acción");
    }

    // Invariants rejected by domain value objects (Email, Phone...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Datos inválidos", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Sesión inválida", ex.getMessage());
    }

    @ExceptionHandler(OwnerNotFoundException.class)
    public ProblemDetail handleOwnerNotFound(OwnerNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ProviderNotFoundException.class)
    public ProblemDetail handleProviderNotFound(ProviderNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(RoleNotAllowedException.class)
    public ProblemDetail handleRoleNotAllowed(RoleNotAllowedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(OfferingNotFoundException.class)
    public ProblemDetail handleOfferingNotFound(OfferingNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(NotOfferingOwnerException.class)
    public ProblemDetail handleNotOfferingOwner(NotOfferingOwnerException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(TimeSlotOverlapException.class)
    public ProblemDetail handleTimeSlotOverlap(TimeSlotOverlapException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(PetNotFoundException.class)
    public ProblemDetail handlePetNotFound(PetNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(TimeSlotNotFoundException.class)
    public ProblemDetail handleTimeSlotNotFound(TimeSlotNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(PetNotOwnedException.class)
    public ProblemDetail handlePetNotOwned(PetNotOwnedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(TimeSlotUnavailableException.class)
    public ProblemDetail handleTimeSlotUnavailable(TimeSlotUnavailableException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(DuplicateServiceRequestException.class)
    public ProblemDetail handleDuplicateServiceRequest(DuplicateServiceRequestException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(ServiceRequestNotFoundException.class)
    public ProblemDetail handleServiceRequestNotFound(ServiceRequestNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(NotRequestProviderException.class)
    public ProblemDetail handleNotRequestProvider(NotRequestProviderException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(NotRequestParticipantException.class)
    public ProblemDetail handleNotRequestParticipant(NotRequestParticipantException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(RequestNotPendingException.class)
    public ProblemDetail handleRequestNotPending(RequestNotPendingException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(OwnerAlreadyExistsException.class)
    public ProblemDetail handleOwnerAlreadyExists(OwnerAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(PetAlreadyExistsException.class)
    public ProblemDetail handlePetAlreadyExists(PetAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(OfferingAlreadyExistsException.class)
    public ProblemDetail handleOfferingAlreadyExists(OfferingAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(ProviderAlreadyExistsException.class)
    public ProblemDetail handleProviderAlreadyExists(ProviderAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    // Unique constraint hit, e.g. two concurrent registrations with the same email
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Conflicto",
                "No fue posible completar la operación por un conflicto con los datos existentes");
    }

    // Fallback: never leak internal details to the client
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
