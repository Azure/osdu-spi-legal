// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.middleware;

import tools.jackson.core.JacksonException;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.ServletWebRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javassist.NotFoundException;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionMapper extends ResponseEntityExceptionHandler {

	private static final Gson gson = new Gson();
	private static final Pattern UNRECOGNIZED_FIELD = Pattern.compile("Unrecognized (?:field|property) \"([^\"]+)\"");
	private static final Pattern JAVA_TYPE = Pattern.compile("(?:[a-z_][a-z0-9_]*\\.)+[A-Za-z_][A-Za-z0-9_$.<>]*");

	@Inject
	private JaxRsDpsLog jaxRsDpsLogger;

	@ExceptionHandler(AppException.class)
	protected ResponseEntity<Object> handleAppException(AppException e) {
		return this.getErrorResponse(e);
	}

	@ExceptionHandler(NotFoundException.class)
	protected ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.NOT_FOUND.value(), "Resource not found.", e.getMessage()));
	}

	@ExceptionHandler(JacksonException.class)
	protected ResponseEntity<Object> handleJacksonException(JacksonException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Bad JSON format", e.getMessage()));
	}

	@ExceptionHandler(UnrecognizedPropertyException.class)
	protected ResponseEntity<Object> handleUnrecognizedPropertyException(UnrecognizedPropertyException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Unrecognized fields found on request",
						e.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.FORBIDDEN.value(), "Access is denied.", e.getMessage()));
	}

	@ExceptionHandler(ValidationException.class)
	protected ResponseEntity<Object> handleValidationException(ValidationException e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Validation error.", e.getMessage()));
	}

	// Tomcat 11 throws this lazily when the controller reads a malformed query string
	// (e.g. a parameter with an empty name); Tomcat 10.1 silently skipped such chunks.
	@ExceptionHandler(InvalidParameterException.class)
	protected ResponseEntity<Object> handleInvalidParameterException(InvalidParameterException e) {
		int status = e.getErrorCode() >= 400 && e.getErrorCode() < 500
				? e.getErrorCode() : HttpStatus.BAD_REQUEST.value();
		return this.getErrorResponse(
				new AppException(status, "Invalid request parameters.",
						"The request contains malformed query or form parameters.", e));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	protected ResponseEntity<Object> handleConstraintValidationException(ConstraintViolationException e) {
		jaxRsDpsLogger.error("Validation exception", e);

		List<String> msgs = new ArrayList<String>();
		for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
			msgs.add(violation.getMessage());
		}
		return this.validationErrorsResponse(msgs);
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<Object> handleIOException(IOException e) {
		if (StringUtils.containsIgnoreCase(ExceptionUtils.getRootCauseMessage(e), "Broken pipe")) {
			this.jaxRsDpsLogger.warning("Client closed the connection while request still being processed");
			return null;
		} else {
			return this.getErrorResponse(
					new AppException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Unknown error", e.getMessage(), e));
		}
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<Object> handleGeneralException(Exception e) {
		return this.getErrorResponse(
				new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error.",
						"An unknown error has occurred.", e.getMessage(), e));
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			@NonNull HttpMessageNotReadableException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {
		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Bad Request", readableJsonMessage(e), e));
	}

	/**
	 * Field path plus a short reason. Jackson's original message names Java types
	 * ({@code java.lang.String}, DTO classes); those stay out of the response.
	 */
	static String readableJsonMessage(HttpMessageNotReadableException e) {
		JacksonException mappingException = findJacksonException(e);
		if (mappingException != null) {
			String path = jsonFieldPath(mappingException);
			String reason = shortJsonReason(mappingException.getOriginalMessage());
			if (StringUtils.isNotBlank(path)) {
				if (StringUtils.isNotBlank(reason)) {
					return String.format("Failed to read request at %s: %s", path, reason);
				}
				return String.format("Failed to read request at %s", path);
			}
			if (StringUtils.isNotBlank(reason)) {
				return "Failed to read request: " + reason;
			}
		}
		Throwable cause = e.getMostSpecificCause();
		if (cause != null && StringUtils.isNotBlank(cause.getMessage())) {
			String reason = shortJsonReason(cause.getMessage());
			if (StringUtils.isNotBlank(reason)) {
				return reason;
			}
		}
		return "Failed to read request";
	}

	private static String jsonFieldPath(JacksonException exception) {
		StringJoiner path = new StringJoiner(".");
		for (JacksonException.Reference reference : exception.getPath()) {
			if (reference.getPropertyName() != null) {
				path.add(reference.getPropertyName());
			} else if (reference.getIndex() >= 0) {
				path.add(Integer.toString(reference.getIndex()));
			}
		}
		return path.toString();
	}

	static String shortJsonReason(String detail) {
		if (StringUtils.isBlank(detail)) {
			return null;
		}
		Matcher unrecognized = UNRECOGNIZED_FIELD.matcher(detail);
		if (unrecognized.find()) {
			return "unknown property \"" + unrecognized.group(1) + "\"";
		}
		String lower = detail.toLowerCase(Locale.ROOT);
		if (lower.contains("null")) {
			return "null is not allowed";
		}
		if (containsAny(detail, "Number value", "VALUE_NUMBER", "Floating-point")) {
			return "wrong JSON type (number)";
		}
		if (containsAny(detail, "Boolean", "VALUE_TRUE", "VALUE_FALSE")) {
			return "wrong JSON type (boolean)";
		}
		if (containsAny(detail, "Array value", "START_ARRAY")) {
			return "wrong JSON type (array)";
		}
		if (containsAny(detail, "Object value", "START_OBJECT")) {
			return "wrong JSON type (object)";
		}
		if (containsAny(detail, "not a valid", "Unexpected token", "Cannot deserialize", "Cannot construct",
				"Cannot map")) {
			return "invalid value";
		}
		if (JAVA_TYPE.matcher(detail).find()) {
			return "invalid value";
		}
		String oneLine = detail.replaceAll("\\s+", " ").trim();
		return oneLine.length() > 160 ? "invalid value" : oneLine;
	}

	private static boolean containsAny(String detail, String... needles) {
		for (String needle : needles) {
			if (detail.contains(needle)) {
				return true;
			}
		}
		return false;
	}

	private static JacksonException findJacksonException(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof JacksonException mappingException) {
				return mappingException;
			}
			current = current.getCause();
		}
		return null;
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleTypeMismatch(
			@NonNull TypeMismatchException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {
		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Bad Request",
						"Invalid parameters were given on request", e));
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
			@NonNull HttpRequestMethodNotSupportedException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {
		return this.getErrorResponse(
				new AppException(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method not found.",
						"Method not found.", e));
	}

	@Override
	@NonNull
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			@NonNull MethodArgumentNotValidException e,
			@NonNull HttpHeaders headers,
			@NonNull HttpStatusCode status,
			@NonNull WebRequest request) {

		List<String> errorMessages = new ArrayList<>();
		e.getBindingResult().getAllErrors().forEach(objectError -> {
			String errorMessage = objectError.getDefaultMessage();
			if (errorMessage != null) {
				errorMessages.add(errorMessage);
			}
		});

		return this.validationErrorsResponse(errorMessages);
	}

	/**
	 * {@link ResponseEntityExceptionHandler} still emits RFC 7807 bodies for any
	 * method that is not overridden (415, 406, ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦). Map those through
	 * {@link AppException} instead.
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		if (request instanceof ServletWebRequest servletWebRequest) {
			HttpServletResponse response = servletWebRequest.getResponse();
			if (response != null && response.isCommitted()) {
				return null;
			}
		}
		if (body instanceof AppError) {
			return new ResponseEntity<>(body, headers, status);
		}
		HttpStatus httpStatus = HttpStatus.resolve(status.value());
		String reason = httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
		String message = ex.getMessage() != null ? ex.getMessage() : reason;
		ResponseEntity<Object> mapped = this.getErrorResponse(
				new AppException(status.value(), reason, message, ex));
		return new ResponseEntity<>(mapped.getBody(), headers, mapped.getStatusCode());
	}

	private ResponseEntity<Object> validationErrorsResponse(List<String> messages) {
		List<String> msgs = new ArrayList<>(messages);
		if (msgs.isEmpty()) {
			msgs.add("Invalid payload");
		}

		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Validation error.", gson.toJson(Map.of("errors", msgs))));
	}

	public ResponseEntity<Object> getErrorResponse(AppException e) {

		String exceptionMsg = e.getError().getMessage();

		if (e.getError().getCode() > 499) {
			this.jaxRsDpsLogger.error(exceptionMsg, e);
		} else {
			this.jaxRsDpsLogger.warning(exceptionMsg, e);
		}

		return new ResponseEntity<Object>(e.getError(), HttpStatus.resolve(e.getError().getCode()));
	}
}
