package org.opengroup.osdu.legal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/jobs")
@Tag(name = "legaltag-status-job", description = "LegalTags status Job related endpoints")
public interface LegalTagStatusJobApi {
    @Operation(summary = "${legalTagStatusJobApi.checkLegalTagStatusChanges.summary}", description = "${legalTagStatusJobApi.checkLegalTagStatusChanges.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag-status-job"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = HttpStatus.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_CRON + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/updateLegalTagStatus")
    ResponseEntity<HttpStatus> checkLegalTagStatusChanges();
}
