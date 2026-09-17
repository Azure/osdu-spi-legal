package org.opengroup.osdu.legal.tags.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Represents a single invalid LegalTag.")
public class InvalidTagWithReason {

    @Schema(description = "The name of the LegalTag.")
    private String name;

    @Schema(description = "The reason the LegalTag is currently invalid.")
    private String reason;
}
