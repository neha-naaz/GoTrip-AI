package com.tripflow.common.exception;

import com.tripflow.booking.exception.BookingNotAllowedException;
import com.tripflow.booking.exception.BookingNotFoundException;
import com.tripflow.booking.exception.TripFullException;
import com.tripflow.group.exception.TripGroupNotFoundException;
import com.tripflow.payment.exception.PaymentNotAllowedException;
import com.tripflow.payment.exception.PaymentNotFoundException;
import com.tripflow.payment.exception.WebhookUnauthorizedException;
import com.tripflow.trip.exception.AgencyNotVerifiedException;
import com.tripflow.trip.exception.AgencyProfileNotFoundException;
import com.tripflow.trip.exception.DuplicateItineraryDayException;
import com.tripflow.trip.exception.ForbiddenException;
import com.tripflow.trip.exception.InvalidTripStateException;
import com.tripflow.trip.exception.TripDeletionNotAllowedException;
import com.tripflow.trip.exception.TripNotFoundException;
import com.tripflow.trip.exception.TripRulesInvalidException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRegistration(InvalidRegistrationException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotActive(AccountNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(ForbiddenException exception) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", exception.getMessage());
    }

    @ExceptionHandler(AgencyNotVerifiedException.class)
    public ResponseEntity<ProblemDetail> handleAgencyNotVerified(AgencyNotVerifiedException exception) {
        return problem(HttpStatus.FORBIDDEN, "Agency not verified", exception.getMessage());
    }

    @ExceptionHandler(AgencyProfileNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAgencyProfileNotFound(AgencyProfileNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Agency profile not found", exception.getMessage());
    }

    @ExceptionHandler(TripNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTripNotFound(TripNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Trip not found", exception.getMessage());
    }

    @ExceptionHandler(InvalidTripStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTripState(InvalidTripStateException exception) {
        return problem(HttpStatus.CONFLICT, "Invalid trip state", exception.getMessage());
    }

    @ExceptionHandler(DuplicateItineraryDayException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateItineraryDay(DuplicateItineraryDayException exception) {
        return problem(HttpStatus.CONFLICT, "Duplicate itinerary day", exception.getMessage());
    }

    @ExceptionHandler(TripRulesInvalidException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(TripRulesInvalidException exception) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violation", exception.getMessage());
    }

    @ExceptionHandler(TripDeletionNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleTripDeletionNotAllowed(TripDeletionNotAllowedException exception) {
        return problem(HttpStatus.CONFLICT, "Trip deletion not allowed", exception.getMessage());
    }

    @ExceptionHandler(TripFullException.class)
    public ResponseEntity<ProblemDetail> handleTripFull(TripFullException exception) {
        return problem(HttpStatus.CONFLICT, "Trip full", exception.getMessage());
    }

    @ExceptionHandler(BookingNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleBookingNotAllowed(BookingNotAllowedException exception) {
        return problem(HttpStatus.CONFLICT, "Booking not allowed", exception.getMessage());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleBookingNotFound(BookingNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Booking not found", exception.getMessage());
    }

    @ExceptionHandler(PaymentNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handlePaymentNotAllowed(PaymentNotAllowedException exception) {
        return problem(HttpStatus.CONFLICT, "Payment not allowed", exception.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePaymentNotFound(PaymentNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Payment not found", exception.getMessage());
    }

    @ExceptionHandler(WebhookUnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleWebhookUnauthorized(WebhookUnauthorizedException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Webhook unauthorized", exception.getMessage());
    }

    @ExceptionHandler(TripGroupNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTripGroupNotFound(TripGroupNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Trip group not found", exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
