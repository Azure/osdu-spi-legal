package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.constraints.Min;
import org.opengroup.osdu.core.common.model.storage.validation.ValidationDoc;
import org.opengroup.osdu.legal.config.RejectUnknownJsonProperties;
import org.opengroup.osdu.legal.config.StrictIntDeserializer;
import org.opengroup.osdu.legal.config.StrictStringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_UNION_OPERATOR;

@Data
@AllArgsConstructor
// Jackson 3 would otherwise bind through the all-args constructor and drop the field defaults.
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Schema(description = "Represents the Search Query objects for Legaltags.",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public class QueryLegalTag {

    @NotNull
    @ArraySchema(arraySchema = @Schema(description = "Filter condition query"),
            schema = @Schema(type = "string", nullable = false, minLength = 1))
    @JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
    @JsonDeserialize(contentUsing = StrictStringDeserializer.class)
    private List<@NotNull String> queryList;

    @ArraySchema(arraySchema = @Schema(
            description = "Logical operators for joining queryList conditions. "
                    + "Only the first element is used. It is matched case-insensitively as a substring "
                    + "against 'union', 'intersection', or 'add' (same as runtime). "
                    + "When omitted or empty, union is assumed."),
            schema = @Schema(type = "string", example = LEGAL_QUERY_API_UNION_OPERATOR))
    @JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
    @JsonDeserialize(contentUsing = StrictStringDeserializer.class)
    private List<String> operatorList;

    @Schema(description = "The field to sort results by")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String sortBy;

    @Schema(description = "The sort order, e.g. 'ASC' or 'DESC'")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String sortOrder;

    @Min(0)
    // Avoid @Schema(type="integer"): OAS 3.1 swagger-core emits type=string for that combination.
    @Schema(description = "The maximum number of LegalTags to return. Must be a JSON number (not a string).",
            format = "int32",
            minimum = "0",
            maximum = ValidationDoc.INT32_MAXIMUM)
    @JsonDeserialize(using = StrictIntDeserializer.class)
    private int limit;

    @JsonAnySetter
    public void rejectUnknownProperty(String name, Object value) throws UnrecognizedPropertyException {
        RejectUnknownJsonProperties.reject(this, name);
    }
}
