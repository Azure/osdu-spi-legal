package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@AllArgsConstructor
// Jackson 3 would otherwise bind through the all-args constructor and drop the field defaults.
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Schema(description = "Represents a collection of invalid LegalTags.")
public class InvalidTagsWithReason {

    @Schema(description = "A collection of invalid LegalTags")
    private Collection<InvalidTagWithReason> invalidLegalTags = new ArrayList<>();
}
