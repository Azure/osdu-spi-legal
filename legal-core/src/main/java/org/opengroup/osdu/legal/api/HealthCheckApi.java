package org.opengroup.osdu.legal.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.annotation.security.PermitAll;

@RequestMapping("/_ah")
@Tag(name = "health", description = "Health related endpoints")
public interface HealthCheckApi {
    @Operation(summary = "${healthCheckApi.livenessCheck.summary}",
            description = "${healthCheckApi.livenessCheck.description}", tags = {"health"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    // Public endpoint: empty @SecurityRequirements clears global bearer so the spec has security: [].
    @SecurityRequirements
    @PermitAll
    // No produces: probes often send Accept: text/plain; constraining to application/json would 406 them.
    @GetMapping("/liveness_check")
    ResponseEntity<Void> livenessCheck();

    @Operation(summary = "${healthCheckApi.readinessCheck.summary}",
            description = "${healthCheckApi.readinessCheck.description}", tags = {"health"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @SecurityRequirements
    @PermitAll
    @GetMapping("/readiness_check")
    ResponseEntity<Void> readinessCheck();
}
