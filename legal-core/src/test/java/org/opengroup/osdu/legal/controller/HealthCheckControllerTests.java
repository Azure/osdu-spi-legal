package org.opengroup.osdu.legal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.controller.HealthCheckController;
import org.springframework.http.HttpStatus;

public class HealthCheckControllerTests {

	private HealthCheckController sut;

	@BeforeEach
	public void setup() {
		this.sut = new HealthCheckController();
	}

	@Test
	public void should_returnHttp200_when_checkLiveness() {
		assertEquals(HttpStatus.OK, this.sut.livenessCheck().getStatusCode());
	}

	@Test
	public void should_returnHttp200_when_checkReadiness() {
		assertEquals(HttpStatus.OK, this.sut.readinessCheck().getStatusCode());
	}
}
