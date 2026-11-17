package org.opengroup.osdu.legal.middleware;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.opengroup.osdu.legal.tags.dto.CreateLegalTag;
import org.opengroup.osdu.legal.tags.dto.RequestLegalTags;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.createBody;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.mockito.InjectMocks;
import org.apache.tomcat.util.http.InvalidParameterException;
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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

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
		assertEquals(409, response.getStatusCode().value());
		// assertEquals(MediaType.APPLICATION_JSON,
		// response.getHeaders().getContentType().toString());
		AppError expectedError = new AppError(409, "any reason", "any message");
		assertEquals(expectedError, response.getBody());
	}

	@Test
	public void should_addLocationHeader_when_fromAppException() {

		AppException exception = new AppException(302, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);
		assertEquals(302, response.getStatusCode().value());
	}

	@Test
	public void should_useGenericResponse_when_exceptionIsThrownDuringMapping() {

		AppException exception = new AppException(302, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);

		assertEquals(302, response.getStatusCode().value());
		// assertEquals(MediaType.APPLICATION_JSON,
		// response.getHeaders().getContentType().toString());
		// assertEquals("any message", response.getBody());
	}

	@Test
	public void should_use404ValueInResponse_When_NotFoundExceptionIsHandledByGlobalExceptionMapper() {

		NotFoundException exception = new NotFoundException("any message");
		ResponseEntity<Object> response = sut.handleNotFoundException(exception);
		assertEquals(404, response.getStatusCode().value());
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
		assertEquals(405, response.getStatusCode().value());
	}

	@Test
	public void should_returnAppError_when_unoverriddenMvcExceptionGoesThroughHandleExceptionInternal() {
		HttpMediaTypeNotSupportedException unsupported =
				new HttpMediaTypeNotSupportedException("application/xml");
		ResponseEntity<Object> unsupportedResponse = sut.handleExceptionInternal(
				unsupported, null, new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE, mock(WebRequest.class));

		assertEquals(415, unsupportedResponse.getStatusCode().value());
		AppError unsupportedError = (AppError) unsupportedResponse.getBody();
		assertEquals(415, unsupportedError.getCode());
		assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), unsupportedError.getReason());

		HttpMediaTypeNotAcceptableException notAcceptable =
				new HttpMediaTypeNotAcceptableException("Could not find acceptable representation");
		ResponseEntity<Object> notAcceptableResponse = sut.handleExceptionInternal(
				notAcceptable, null, new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE, mock(WebRequest.class));

		assertEquals(406, notAcceptableResponse.getStatusCode().value());
		AppError notAcceptableError = (AppError) notAcceptableResponse.getBody();
		assertEquals(406, notAcceptableError.getCode());
		assertEquals(HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(), notAcceptableError.getReason());
	}

	@Test
	public void should_use400ValueInResponse_When_MethodArgumentNotValidExceptionIsHandledByGlobalExceptionMapper() {
		HttpHeaders httpHeaders = new HttpHeaders();
		WebRequest webRequest = mock(WebRequest.class);

		BindingResult bindingResult = mock(BindingResult.class);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
		when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());
		ResponseEntity<Object> response = sut.handleMethodArgumentNotValid(exception, httpHeaders,
				HttpStatus.BAD_REQUEST, webRequest);
		assertEquals(400, response.getStatusCode().value());
		AppError error = (AppError) response.getBody();
		assertEquals("Validation error.", error.getReason());
		assertEquals(json(Map.of("errors", List.of("Invalid payload"))), error.getMessage());

	}

	@Test
	public void should_sanitizeErrorMessages_When_MethodArgumentNotValidExceptionIsHandledByGlobalExceptionMapper() {
		HttpHeaders httpHeaders = new HttpHeaders();
		WebRequest webRequest = mock(WebRequest.class);
		FieldError fieldError = new FieldError("objectName", "fieldName",
				"log_injection_before_3773793\r\nlog_injection_after_3773793");
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
		when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

		ResponseEntity<Object> response = sut.handleMethodArgumentNotValid(exception, httpHeaders,
				HttpStatus.BAD_REQUEST, webRequest);

		assertEquals(400, response.getStatusCode().value());
	
		AppError error = (AppError) response.getBody();
		assertEquals("Validation error.", error.getReason());
		assertTrue(error.getMessage().startsWith("{\"errors\":["));
		assertTrue(error.getMessage().contains("log_injection_before_3773793"));
		assertTrue(error.getMessage().contains("log_injection_after_3773793"));
		assertTrue(!error.getMessage().contains("\r") && !error.getMessage().contains("\n"),
				"validation message JSON must not contain raw CR/LF");
	}

	@Test
	public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {

		Exception exception = new Exception("any message");

		ResponseEntity<Object> response = sut.handleGeneralException(exception);
		assertEquals(500, response.getStatusCode().value());
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

		assertEquals(org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusCode().value());
	}

	@Test
	public void should_returnBadRequest_when_TomcatInvalidParameterExceptionIsCaptured() {
		InvalidParameterException exception = new InvalidParameterException(
				"Invalid chunk starting at byte [0] and ending at byte [4] with a value of [=abc] ignored");

		ResponseEntity response = sut.handleInvalidParameterException(exception);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCode().value());
	}

	@Test
	public void should_returnBadRequest_when_JacksonExceptionIsCaptured() {
		JacksonException jacksonException = mock(JacksonException.class);
		when(jacksonException.getMessage()).thenReturn("JsonProcessing exception");

		ResponseEntity response = sut.handleJacksonException(jacksonException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCode().value());
	}

	@Test
	public void should_includeJsonPath_when_HttpMessageNotReadableFromMappingFailure() throws Exception {
		DatabindException mappingException;
		try {
			new ObjectMapper().readValue(
					json(withField(withField(createBody(), "description", null), "properties", Map.of())),
					CreateLegalTag.class);
			throw new AssertionError("expected mapping failure");
		} catch (DatabindException e) {
			mappingException = e;
		}

		HttpMessageNotReadableException notReadable =
				new HttpMessageNotReadableException("JSON parse error", mappingException, null);
		HttpHeaders httpHeaders = mock(HttpHeaders.class);
		WebRequest webRequest = mock(WebRequest.class);

		ResponseEntity<Object> response = sut.handleHttpMessageNotReadable(
				notReadable, httpHeaders, HttpStatus.BAD_REQUEST, webRequest);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		AppError error = (AppError) response.getBody();
		assertEquals("Failed to read request at description: null is not allowed", error.getMessage());
		assertFalse(error.getMessage().contains("java."));
		assertFalse(error.getMessage().contains("org.opengroup"));
	}

	@Test
	public void should_hideJavaType_when_HttpMessageNotReadableIsWrongJsonType() throws Exception {
		DatabindException mappingException;
		try {
			new ObjectMapper().readValue(json(Map.of("name", 12345)), CreateLegalTag.class);
			throw new AssertionError("expected mapping failure");
		} catch (DatabindException e) {
			mappingException = e;
		}

		String message = GlobalExceptionMapper.readableJsonMessage(
				new HttpMessageNotReadableException("JSON parse error", mappingException, null));

		assertTrue(message.startsWith("Failed to read request at name:"));
		assertFalse(message.contains("java."));
		assertFalse(message.contains("org.opengroup"));
	}

	@Test
	public void should_includeArrayIndex_when_HttpMessageNotReadableHasNestedPath() {
		DatabindException cause = assertThrows(DatabindException.class,
				() -> new ObjectMapper().readValue(json(Map.of("names", List.of(false))), RequestLegalTags.class));

		String message = GlobalExceptionMapper.readableJsonMessage(
				new HttpMessageNotReadableException("JSON parse error", cause, null));

		assertEquals("Failed to read request at names.0: wrong JSON type (boolean)", message);
	}

	@Test
	public void should_useCauseOrWrapperMessage_when_HttpMessageNotReadableHasNoMappingPath() {
		HttpMessageNotReadableException notReadable =
				new HttpMessageNotReadableException("JSON parse error", null, null);

		assertEquals("JSON parse error", GlobalExceptionMapper.readableJsonMessage(notReadable));
	}

	@Test
	public void should_returnBadRequest_when_UnrecognizedPropertyExceptionIsCaptured() {
		UnrecognizedPropertyException unrecognizedPropertyException = mock(UnrecognizedPropertyException.class);
		when(unrecognizedPropertyException.getMessage()).thenReturn("Unrecognized exception");

		ResponseEntity response = sut.handleUnrecognizedPropertyException(unrecognizedPropertyException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCode().value());
	}

	@Test
	public void should_returnForbidden_when_AccessDeniedExceptionIsCaptured() {
		AccessDeniedException accessDeniedException = mock(AccessDeniedException.class);
		when(accessDeniedException.getMessage()).thenReturn("Access Denied exception");

		ResponseEntity response = sut.handleAccessDeniedException(accessDeniedException);

		assertEquals(org.apache.http.HttpStatus.SC_FORBIDDEN, response.getStatusCode().value());
	}

	@Test
	public void should_returnBadRequest_when_ValidationExceptionIsCaptured() {
		ValidationException validationException = mock(ValidationException.class);
		when(validationException.getMessage()).thenReturn("Validation exception");

		ResponseEntity response = sut.handleValidationException(validationException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCode().value());
	}

	@Test
	public void should_returnBadRequest_when_ConstraintViolationExceptionWithMessageIsCaptured() {
		ConstraintViolationException constraintViolationException = mock(ConstraintViolationException.class);
		ConstraintViolation constraintViolation = mock(ConstraintViolation.class);

		when(constraintViolationException.getConstraintViolations())
				.thenReturn(Collections.singleton(constraintViolation));
		when(constraintViolation.getMessage()).thenReturn("custom constraint validation");

		ResponseEntity response = sut.handleConstraintValidationException(constraintViolationException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCode().value());
		AppError error = (AppError) response.getBody();
		assertEquals("Validation error.", error.getReason());
		assertEquals(json(Map.of("errors", List.of("custom constraint validation"))), error.getMessage());
	}

	@Test
	public void should_returnBadRequest_when_ConstraintViolationExceptionWithEmptyMessageIsCaptured() {
		ConstraintViolationException constraintViolationException = mock(ConstraintViolationException.class);
		when(constraintViolationException.getConstraintViolations()).thenReturn(Collections.emptySet());

		ResponseEntity response = sut.handleConstraintValidationException(constraintViolationException);

		assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatusCode().value());
		AppError error = (AppError) response.getBody();
		assertEquals("Validation error.", error.getReason());
		assertEquals(json(Map.of("errors", List.of("Invalid payload"))), error.getMessage());
	}

}
