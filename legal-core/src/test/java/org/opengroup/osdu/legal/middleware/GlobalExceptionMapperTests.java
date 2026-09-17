package org.opengroup.osdu.legal.middleware;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import javassist.NotFoundException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlobalExceptionMapperTests {
	@Mock
	private JaxRsDpsLog log;

	@InjectMocks
	private GlobalExceptionMapper sut;

	@Test
	public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() {

		AppException exception = new AppException(409, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);
		assertEquals(409, response.getStatusCodeValue());
		// assertEquals(MediaType.APPLICATION_JSON,
		// response.getHeaders().getContentType().toString());
		AppError expectedError = new AppError(409, "any reason", "any message");
		assertEquals(expectedError, response.getBody());
	}

	@Test
	public void should_addLocationHeader_when_fromAppException() {

		AppException exception = new AppException(302, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);
		assertEquals(302, response.getStatusCodeValue());
	}

	@Test
	public void should_useGenericResponse_when_exceptionIsThrownDuringMapping() {

		AppException exception = new AppException(302, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);

		assertEquals(302, response.getStatusCodeValue());
		// assertEquals(MediaType.APPLICATION_JSON,
		// response.getHeaders().getContentType().toString());
		// assertEquals("any message", response.getBody());
	}

	@Test
	public void should_use404ValueInResponse_When_NotFoundExceptionIsHandledByGlobalExceptionMapper() {

		NotFoundException exception = new NotFoundException("any message");
		ResponseEntity<Object> response = sut.handleNotFoundException(exception);
		assertEquals(404, response.getStatusCodeValue());
		// assertEquals(MediaType.APPLICATION_JSON,
		// response.getHeaders().getContentType().toString());
		// assertEquals(null, response.getBody().getMessage());
	}

	@Test
	public void should_use405ValueInResponse_When_HttpRequestMethodNotSupportedExceptionIsHandledByGlobalExceptionMapper() {
		HttpHeaders httpHeaders = mock(HttpHeaders.class);
		WebRequest webRequest = mock(WebRequest.class);
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("any message");
		ResponseEntity<Object> response = sut.handleHttpRequestMethodNotSupported(exception, httpHeaders,
				HttpStatus.METHOD_NOT_ALLOWED, webRequest);
		assertEquals(405, response.getStatusCodeValue());
	}

	@Test
	public void should_use400ValueInResponse_When_MethodArgumentNotValidExceptionIsHandledByGlobalExceptionMapper() {
		HttpHeaders httpHeaders = new HttpHeaders();
		WebRequest webRequest = mock(WebRequest.class);

		BindingResult bindingResult = mock(BindingResult.class);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
		ResponseEntity<Object> response = sut.handleMethodArgumentNotValid(exception, httpHeaders,
				HttpStatus.BAD_REQUEST, webRequest);
		assertEquals(400, response.getStatusCodeValue());

	}

	@Test
	public void should_sanitizeErrorMessages_When_MethodArgumentNotValidExceptionIsHandledByGlobalExceptionMapper() {
		HttpHeaders httpHeaders = new HttpHeaders();
		WebRequest webRequest = mock(WebRequest.class);
		FieldError fieldError = new FieldError("objectName", "fieldName",
				"log_injection_before_3773793\r\nlog_injection_after_3773793");
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

		ResponseEntity<Object> response = sut.handleMethodArgumentNotValid(exception, httpHeaders,
				HttpStatus.BAD_REQUEST, webRequest);

		assertEquals(400, response.getStatusCodeValue());
	
		AppError error = (AppError) response.getBody();
		assertTrue(error.getMessage().contains("log_injection_before_3773793\\r\\nlog_injection_after_3773793"));
	}

	@Test
	public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {

		Exception exception = new Exception("any message");

		ResponseEntity<Object> response = sut.handleGeneralException(exception);
		assertEquals(500, response.getStatusCodeValue());
		// assertEquals(MediaType.APPLICATION_JSON,
		// response.getHeaders().getContentType().toString());
		// assertEquals("An unknown error has occurred.",
		// response.getBody().getMessage());
	}

	@Test
	public void should_returnNullResponse_when_BrokenPipeIOExceptionIsCaptured() {
		IOException ioException = new IOException("Broken pipe");

		ResponseEntity response = sut.handleIOException(ioException);

		assertNull(response);
	}

	@Test
	public void should_returnServiceUnavailable_when_IOExceptionIsCaptured() {
		IOException ioException = new IOException("Not broken yet");

		ResponseEntity response = sut.handleIOException(ioException);

		assertEquals(org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusCodeValue());
	}

	@Test
	public void should_returnBadRequest_when_JsonProcessingExceptionIsCaptured() {
		JsonProcessingException jsonProcessingException = mock(JsonProcessingException.class);
		when(jsonProcessingException.getMessage()).thenReturn("JsonProcessing exception");

		ResponseEntity response = sut.handleJsonProcessingException(jsonProcessingException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
	}

	@Test
	public void should_returnBadRequest_when_UnrecognizedPropertyExceptionIsCaptured() {
		UnrecognizedPropertyException unrecognizedPropertyException = mock(UnrecognizedPropertyException.class);
		when(unrecognizedPropertyException.getMessage()).thenReturn("Unrecognized exception");

		ResponseEntity response = sut.handleUnrecognizedPropertyException(unrecognizedPropertyException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
	}

	@Test
	public void should_returnForbidden_when_AccessDeniedExceptionIsCaptured() {
		AccessDeniedException accessDeniedException = mock(AccessDeniedException.class);
		when(accessDeniedException.getMessage()).thenReturn("Access Denied exception");

		ResponseEntity response = sut.handleAccessDeniedException(accessDeniedException);

		assertEquals(org.apache.http.HttpStatus.SC_FORBIDDEN, response.getStatusCodeValue());
	}

	@Test
	public void should_returnBadRequest_when_ValidationExceptionIsCaptured() {
		ValidationException validationException = mock(ValidationException.class);
		when(validationException.getMessage()).thenReturn("Validation exception");

		ResponseEntity response = sut.handleValidationException(validationException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
	}

	@Test
	public void should_returnBadRequest_when_ConstraintViolationExceptionWithMessageIsCaptured() {
		ConstraintViolationException constraintViolationException = mock(ConstraintViolationException.class);
		ConstraintViolation constraintViolation = mock(ConstraintViolation.class);

		Set<ConstraintViolation> constraintViolations = new HashSet<>();
		constraintViolations.add(constraintViolation);

		when(constraintViolationException.getConstraintViolations())
				.thenReturn(Collections.singleton(constraintViolation));
		when(constraintViolation.getMessage()).thenReturn("custom constraint validation");

		ResponseEntity response = sut.handleConstraintValidationException(constraintViolationException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
	}

	@Test
	public void should_returnBadRequest_when_ConstraintViolationExceptionWithEmptyMessageIsCaptured() {
		ConstraintViolationException constraintViolationException = mock(ConstraintViolationException.class);

		ResponseEntity response = sut.handleConstraintValidationException(constraintViolationException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
	}

}