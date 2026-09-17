package org.opengroup.osdu.legal.tags.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Represents the Search Query objects for Legaltags.")
public class QueryLegalTag {

    @NotNull
    @Schema(description = "Filter condition query")
    private List<String> queryList;

    @Schema(description = "If there are multiple conditions need to be joined in by logical operators")
    private List<String> operatorList;

    @Schema(description = "The field to sort results by")
    private String sortBy;

    @Schema(description = "The sort order, e.g. 'ASC' or 'DESC'")
    private String sortOrder;

    @Schema(description = "The maximum number of LegalTags to return")
    private int limit;
}
