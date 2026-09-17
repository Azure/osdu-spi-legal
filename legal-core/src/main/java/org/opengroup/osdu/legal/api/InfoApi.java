package org.opengroup.osdu.legal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@RequestMapping
@Tag(name = "info", description = "Version info endpoint")
public interface InfoApi {
    @Operation(summary = "${infoApi.info.summary}", description = "${infoApi.info.description}", tags = {"info"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Version info.", content = {@Content(schema = @Schema(implementation = VersionInfo.class))})
    })
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    VersionInfo info() throws IOException;
}
