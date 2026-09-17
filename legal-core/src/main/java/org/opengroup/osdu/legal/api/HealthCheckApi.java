package org.opengroup.osdu.legal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
            @ApiResponse(responseCode = "200", description = "Legal service is alive", content = {@Content(schema = @Schema(implementation = String.class))})
    })
    @PermitAll
    @GetMapping("/liveness_check")
    ResponseEntity<String> livenessCheck();

    @Operation(summary = "${healthCheckApi.readinessCheck.summary}",
            description = "${healthCheckApi.readinessCheck.description}", tags = {"health"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Legal service is ready", content = {@Content(schema = @Schema(implementation = String.class))})
    })
    @PermitAll
    @GetMapping("/readiness_check")
    ResponseEntity<String> readinessCheck();
}
