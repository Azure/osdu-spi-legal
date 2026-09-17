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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javassist.NotFoundException;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.apache.commons.text.StringEscapeUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

	@ExceptionHandler(JsonProcessingException.class)
	protected ResponseEntity<Object> handleJsonProcessingException(JsonProcessingException e) {
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

	@ExceptionHandler(ConstraintViolationException.class)
	protected ResponseEntity<Object> handleConstraintValidationException(ConstraintViolationException e) {
		jaxRsDpsLogger.error("Validation exception", e);

		List<String> msgs = new ArrayList<String>();
		for (ConstraintViolation violation : e.getConstraintViolations()) {
			msgs.add(violation.getMessage());
		}
		if (msgs.isEmpty()) {
			msgs.add("Invalid payload");
		}

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode array = mapper.valueToTree(msgs);
		JsonNode result = mapper.createObjectNode().set("errors", array);

		return this.getErrorResponse(
				new AppException(HttpStatus.BAD_REQUEST.value(), "Validation error.", result.toString()));
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

		List<String> sanitizedErrorMessages = new ArrayList<>();
		e.getBindingResult().getFieldErrors().forEach(fieldError -> {
			String errorMessage = fieldError.getDefaultMessage();
			String sanitizedMessage = StringEscapeUtils.escapeJava(errorMessage);
			sanitizedErrorMessages.add(sanitizedMessage);
		});

		String errorsString = String.join("; ", sanitizedErrorMessages);

		AppException appException = new AppException(
				HttpStatus.BAD_REQUEST.value(),
				"Validation failed.",
				errorsString);

		return this.getErrorResponse(appException);
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
