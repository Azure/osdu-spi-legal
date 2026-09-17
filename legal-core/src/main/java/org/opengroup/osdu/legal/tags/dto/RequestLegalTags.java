package org.opengroup.osdu.legal.tags.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "The model to retrieve multiple LegalTags in batch.")
public class RequestLegalTags {

    @Schema(description = "The name of all the LegalTags to retrieve.")
    @NotNull
    @Size(min=1, max=25)
    @Valid
    List<@ValidName String> names = new ArrayList<>();
}
