package org.opengroup.osdu.legal.tags.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@Schema(description = "Shows the allowed values of the fields of a LegalTag.")
public class ReadablePropertyValues {

    @Schema(description = "The values of all the allowed Countries of Origin with the ISO Alpha 2 code and country name.")
    private Map<String, String> countriesOfOrigin;

    @Schema(description = "The values of all the allowed Other Relevant Data Countries with the ISO Alpha 2 code and country name.")
    private Map<String, String> otherRelevantDataCountries;

    @Schema(description = "The values of all the allowed Security Classifications.")
    private Set<String> securityClassifications;

    @Schema(description = "The name of all the allowed Export Classifications.")
    private Set<String> exportClassificationControlNumbers;

    @Schema(description = "The name of all the allowed Personal Data Type values.")
    private Set<String> personalDataTypes;

    @Schema(description = "The name of all the allowed Data Type values.")
    private Set<String> dataTypes;
}
