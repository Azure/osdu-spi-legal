package org.opengroup.osdu.legal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.AppError;

public class AppExceptionTests {

	@Test
	public void constructorTest() {
		AppException exception = new AppException(200, "unknown error", "this error occurred:");
		assertNotNull(exception);

		AppError error = exception.getError();
		assertNotNull(error);

		assertEquals(200, error.getCode());
		assertEquals("unknown error", error.getReason());
		assertEquals("this error occurred:", error.getMessage());
	}
}
