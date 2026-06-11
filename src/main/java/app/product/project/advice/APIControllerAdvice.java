package app.product.project.advice;

import app.product.project.exception.ConflictException;
import app.product.project.exception.InvalidFileException;
import app.product.project.exception.CloudStorageException;
import app.product.project.model.dto.response.ApiDataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class APIControllerAdvice {

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiDataResponse<Object>> handleBadCredentials(@SuppressWarnings("unused") BadCredentialsException e) {
		return new ResponseEntity<>(new ApiDataResponse<>(
				false,
				"Sai tài khoản hoặc mật khẩu",
				null,
				null,
				HttpStatus.UNAUTHORIZED
		), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ApiDataResponse<Object>> handleDisabledAccount(@SuppressWarnings("unused") DisabledException e) {
		return new ResponseEntity<>(new ApiDataResponse<>(
				false,
				"Tài khoản bị khóa (Inactive)",
				null,
				null,
				HttpStatus.FORBIDDEN
		), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiDataResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException e) {
		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		return new ResponseEntity<>(new ApiDataResponse<>(
				false,
				"Lỗi xác thực dữ liệu",
				errors,
				null,
				HttpStatus.BAD_REQUEST
		), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiDataResponse<Object>> handleConflictException(ConflictException e) {
		return new ResponseEntity<>(new ApiDataResponse<>(
				false,
				e.getMessage(),
				null,
				null,
				HttpStatus.CONFLICT
		), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(InvalidFileException.class)
	public ResponseEntity<ApiDataResponse<Object>> handleInvalidFileException(InvalidFileException e) {
		return new ResponseEntity<>(new ApiDataResponse<>(
				false,
				e.getMessage(),
				null,
				null,
				HttpStatus.BAD_REQUEST
		), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CloudStorageException.class)
	public ResponseEntity<ApiDataResponse<Object>> handleCloudStorageException(CloudStorageException e) {
		return new ResponseEntity<>(new ApiDataResponse<>(
				false,
				e.getMessage(),
				null,
				null,
				HttpStatus.SERVICE_UNAVAILABLE
		), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiDataResponse<Object>> handleRuntimeException(RuntimeException e) {
		return new ResponseEntity<>(new ApiDataResponse<>(
				false,
				e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống",
				null,
				null,
				HttpStatus.INTERNAL_SERVER_ERROR
		), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
