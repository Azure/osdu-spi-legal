package org.opengroup.osdu.legal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import org.opengroup.osdu.legal.tags.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping(path = "/")
@Validated
@Tag(name = "legaltag", description = "LegalTags related endpoints")
public interface LegalTagApi {
    @Operation(summary = "${legalTagApi.createLegalTag.summary}", description = "${legalTagApi.createLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created LegalTag successfully.", content = {@Content(schema = @Schema(implementation = LegalTagDto.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "409", description = "A LegalTag with the given name already exists.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags")
    ResponseEntity<LegalTagDto> createLegalTag(@NotNull @RequestBody LegalTagDto legalTag);

    @Operation(summary = "${legalTagApi.updateLegalTag.summary}", description = "${legalTagApi.updateLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated LegalTag successfully.", content = {@Content(schema = @Schema(implementation = LegalTagDto.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag to update was not found.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "409", description = "A LegalTag with the given name already exists.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PutMapping("/legaltags")
    ResponseEntity<LegalTagDto> updateLegalTag(@Valid @NotNull @RequestBody UpdateLegalTag legalTag);

    @Operation(summary = "${legalTagApi.listLegalTags.summary}", description = "${legalTagApi.listLegalTags.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTags successfully.", content = {@Content(schema = @Schema(implementation = LegalTagDtos.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag to update was not found.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags")
    ResponseEntity<LegalTagDtos> listLegalTags(@Parameter(description = "If true returns only valid LegalTags, if false returns only invalid LegalTags.  Default value is true.")
                                               @RequestParam(name = "valid", required = false, defaultValue = "true") boolean valid);

    @Operation(summary = "${legalTagApi.getLegalTag.summary}", description = "${legalTagApi.getLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTag successfully.", content = {@Content(schema = @Schema(implementation = LegalTagDto.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag was not found.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags/{name}")
    ResponseEntity getLegalTag(@Parameter(description = "Name of the LegalTag", in = ParameterIn.PATH,
            example = "OSDU-Private-USA-EHC") @PathVariable("name") @ValidName String name);

    @Operation(summary = "${legalTagApi.deleteLegalTag.summary}", description = "${legalTagApi.deleteLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "LegalTag deleted successfully.", content = {@Content(schema = @Schema(implementation = HttpStatus.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag to delete was not found.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_ADMIN + "')")
    @DeleteMapping("/legaltags/{name}")
    ResponseEntity<HttpStatus> deleteLegalTag(@Parameter(description = "Name of the LegalTag to delete",
            in = ParameterIn.PATH, example = "OSDU-Private-USA-EHC") @PathVariable("name") @ValidName String name);

    @Operation(summary = "${legalTagApi.getLegalTags.summary}", description = "${legalTagApi.getLegalTags.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTags successfully.", content = {@Content(schema = @Schema(implementation = LegalTagDtos.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "One or more requested LegalTags were not found.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags:batchRetrieve")
    ResponseEntity<LegalTagDtos> getLegalTags(@Valid @NotNull @RequestBody RequestLegalTags requestedTags);

    @Operation(summary = "${legalTagApi.validateLegalTags.summary}", description = "${legalTagApi.validateLegalTags.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTag names with reason successfully.", content = {@Content(schema = @Schema(implementation = InvalidTagsWithReason.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "LegalTag names were not found.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags:validate")
    ResponseEntity<InvalidTagsWithReason> validateLegalTags(@Valid @NotNull @RequestBody RequestLegalTags requestedTags);

    @Operation(summary = "${legalTagApi.getLegalTagProperties.summary}", description = "${legalTagApi.getLegalTagProperties.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTag properties successfully.", content = {@Content(schema = @Schema(implementation = ReadablePropertyValues.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags:properties")
    ResponseEntity<ReadablePropertyValues> getLegalTagProperties();

    @Operation(summary = "${legalTagApi.queryLegalTag.summary}", description = "${legalTagApi.queryLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"legaltag"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTags successfully.", content = {@Content(schema = @Schema(implementation = LegalTagDtos.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag to update was not found.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "405", description = "Method not allowed. Legal Query API is disabled.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags:query")
    ResponseEntity<LegalTagDtos> queryLegalTag(@Valid @NotNull @RequestBody QueryLegalTag searchInput, @Parameter(description = "If true returns only valid LegalTags, if false returns only invalid LegalTags.  Default value is true.")
    @RequestParam(name = "valid", required = false, defaultValue = "true") boolean valid);


}
