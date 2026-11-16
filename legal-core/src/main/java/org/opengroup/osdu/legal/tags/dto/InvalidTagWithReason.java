package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
// Jackson 3 would otherwise bind through the all-args constructor and drop the field defaults.
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Schema(description = "Represents a single invalid LegalTag.")
public class InvalidTagWithReason {

    @Schema(description = "The name of the LegalTag.")
    private String name;

    @Schema(description = "The reason the LegalTag is currently invalid.")
    private String reason;
}
