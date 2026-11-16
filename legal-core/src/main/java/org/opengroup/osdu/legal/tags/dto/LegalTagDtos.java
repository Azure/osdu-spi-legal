package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/*
    Read only collection of LegalTag model that are sent to the user
 */
@Data
@AllArgsConstructor
// Jackson 3 would otherwise bind through the all-args constructor and drop the field defaults.
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Schema(description = "Represents a collection of LegalTags.")
public class LegalTagDtos {

    @Schema(description = "A collection of complete LegalTags")
    private Collection<LegalTagDto> legalTags = new ArrayList<>();
}
