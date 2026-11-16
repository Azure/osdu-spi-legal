package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.constraints.NotNull;
import org.opengroup.osdu.legal.config.RejectUnknownJsonProperties;
import org.opengroup.osdu.legal.config.StrictObjectMapDeserializer;
import org.opengroup.osdu.legal.config.StrictStringDeserializer;
import java.sql.Date;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.core.common.model.legal.validation.ValidDescription;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;
import org.opengroup.osdu.core.common.model.legal.serialization.ExpirationDateDeserializer;

import static org.opengroup.osdu.core.common.util.SerializationUtils.EXPIRATION_DATE_FORMAT;

/**
 * If any class variable changed here,
 * need to update the corresponding doc model class in SwaggerHelper.java
 *
 * <p>Partial update. {@code name} is required and identifies the tag.
 * {@code contractId}, {@code expirationDate}, {@code description}, and
 * {@code extensionProperties} are optional: omit a field to keep the stored value.
 * JSON {@code null} for {@code expirationDate} is the same as omitting it, so a
 * stored expiration date cannot be cleared.
 */
@Data
@AllArgsConstructor
// Jackson 3 would otherwise bind through the all-args constructor and drop the field defaults.
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Schema(description = "The model to update an existing LegalTag",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public class UpdateLegalTag {

    /**
     * Same allowlist as create ({@code CreateLegalTagProperties}): special literals or
     * alphanumeric / hyphen / dot ids of length 3Ã¢â‚¬â€œ40.
     */
    public static final String CONTRACT_ID_PATTERN =
            "^(Unknown|No Contract Related|[-.A-Za-z0-9]{3,40})$";

    @NotNull
    @ValidName
    @Schema(description = "The name of the LegalTag", example = "OSDU-Private-EHCData",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = LegalTagDto.NAME_PATTERN,
            minLength = LegalTagDto.NAME_MIN_LENGTH,
            maxLength = LegalTagDto.NAME_MAX_LENGTH)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String name;

    @Schema(description = "The Id of the physical contract associated with the data being ingested. "
            + "Allows 'Unknown', 'No Contract Related', or a real contract id. "
            + "Optional: omit to keep the existing value.",
            example = "A1234",
            pattern = CONTRACT_ID_PATTERN,
            minLength = 3,
            maxLength = 40)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String contractId;

    @Schema(description = "Optional description for discoverability. Omit to keep the existing value.")
    @ValidDescription
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String description;

    @Schema(description = "Optional expiration date in the format YYYY-MM-DD. "
            + "Must be a future date when provided. Omit or send null to keep the existing value. "
            + "A stored date cannot be cleared.",
            type = "string", format = "date", nullable = true, example = "2099-12-25")
    @JsonDeserialize(using = ExpirationDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = EXPIRATION_DATE_FORMAT)
    private Date expirationDate;

    @Schema(description = "The optional object field to attach any company specific attributes. "
            + "Omit to keep the existing value.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    @JsonDeserialize(using = StrictObjectMapDeserializer.class)
    private Map<String, Object> extensionProperties;

    @JsonAnySetter
    public void rejectUnknownProperty(String name, Object value) throws UnrecognizedPropertyException {
        RejectUnknownJsonProperties.reject(this, name);
    }
}
