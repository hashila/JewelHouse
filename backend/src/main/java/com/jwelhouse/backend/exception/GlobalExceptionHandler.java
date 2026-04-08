package com.jwelhouse.backend.exception;

import com.jwelhouse.backend.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException ex, HttpServletRequest request) {
		log.warn("Business exception at {}: {}", request.getRequestURI(), ex.getMessage());
		return buildErrorResponse(HttpStatus.EXPECTATION_FAILED, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex,
																	  HttpServletRequest request) {
		String message = "Validation failed";
		FieldError fieldError = ex.getBindingResult().getFieldError();
		if (fieldError != null) {
			message = fieldError.getDefaultMessage();
		}
		log.warn("Validation error at {}: {}", request.getRequestURI(), message);
		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex,
																	  HttpServletRequest request) {
		log.warn("Constraint violation at {}: {}", request.getRequestURI(), ex.getMessage());
		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
		log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI());
	}

	private ResponseEntity<ErrorResponseDTO> buildErrorResponse(HttpStatus status, String message, String path) {
		ErrorResponseDTO response = new ErrorResponseDTO(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path
		);
		return ResponseEntity.status(status).body(response);
	}
}

