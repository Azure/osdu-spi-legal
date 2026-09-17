package org.opengroup.osdu.legal.controller;

import org.opengroup.osdu.legal.api.HealthCheckApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController implements HealthCheckApi {

	@Override
	public ResponseEntity<String> livenessCheck() {
		return new ResponseEntity<>("Legal service is alive", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> readinessCheck() {
		return new ResponseEntity<>("Legal service is ready", HttpStatus.OK);
	}
}
