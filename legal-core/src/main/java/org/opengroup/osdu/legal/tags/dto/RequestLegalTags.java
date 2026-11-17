package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.opengroup.osdu.legal.config.RejectUnknownJsonProperties;
import org.opengroup.osdu.legal.config.StrictStringDeserializer;
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
// Jackson 3 would otherwise bind through the all-args constructor and drop the field defaults.
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Schema(description = "The model to retrieve multiple LegalTags in batch.",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public class RequestLegalTags {

    @NotNull
    @Size(min = 1, max = 25)
    @Valid
    @ArraySchema(
            arraySchema = @Schema(
                    description = "The name of all the LegalTags to retrieve.",
                    requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(
                    type = "string",
                    nullable = false,
                    pattern = LegalTagDto.NAME_PATTERN,
                    minLength = LegalTagDto.NAME_MIN_LENGTH,
                    maxLength = LegalTagDto.NAME_MAX_LENGTH,
                    example = "OSDU-Private-USA-EHC"),
            minItems = 1,
            maxItems = 25)
    @JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
    @JsonDeserialize(contentUsing = StrictStringDeserializer.class)
    private List<@NotNull @ValidName String> names = new ArrayList<>();

    @JsonAnySetter
    public void rejectUnknownProperty(String name, Object value) throws UnrecognizedPropertyException {
        RejectUnknownJsonProperties.reject(this, name);
    }
}
