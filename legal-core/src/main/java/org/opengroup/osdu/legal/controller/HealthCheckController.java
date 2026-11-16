package org.opengroup.osdu.legal.controller;

import org.opengroup.osdu.legal.api.HealthCheckApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController implements HealthCheckApi {

	@Override
	public ResponseEntity<Void> livenessCheck() {
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<Void> readinessCheck() {
		return ResponseEntity.ok().build();
	}
}
