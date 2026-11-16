package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.ValidDescription;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.legal.config.CreateLegalTagPropertiesDeserializer;
import org.opengroup.osdu.legal.config.RejectUnknownJsonProperties;
import org.opengroup.osdu.legal.config.StrictStringDeserializer;
import org.opengroup.osdu.legal.util.HtmlEncodeAdapter;

import java.util.Set;

/**
 * Request body for creating a LegalTag.
 *
 * <p>OpenAPI {@code name} maxLength is {@value #NAME_MAX_LENGTH}, not
 * {@link LegalTagDto#NAME_MAX_LENGTH} (100): create may prefix
 * {@code {data-partition-id}-} before the final max-100 {@code @ValidName} check.
 *
 * <p>{@value #NAME_MAX_LENGTH} only leaves room for a partition id of at most 4 characters
 * ({@code 4 + "-" + 95 = 100}). With a longer partition, a spec-valid create name can still
 * be rejected at runtime after prefixing (covered by the create acceptance test that exceeds
 * the partition budget). Conversely, an already-prefixed name of 96–100 characters is valid
 * at runtime ({@code @ValidName}) but outside this schema.
 *
 * <p>Read-only {@link LegalTag} fields ({@code isValid}, {@code id}) are ignored when present
 * so clients that reuse the shared LegalTag model (e.g. os-core-test) keep working. Other
 * unknown properties are still rejected for OpenAPI {@code additionalProperties: false}.
 */
@Data
@AllArgsConstructor
// Jackson 3 would otherwise bind through the all-args constructor and drop the field defaults.
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@Schema(description = "Request body for creating a LegalTag",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public class CreateLegalTag {

    public static final int NAME_MIN_LENGTH = LegalTagDto.NAME_MIN_LENGTH;
    public static final int NAME_MAX_LENGTH = 95;
    public static final String NAME_PATTERN = "^[-A-Za-z0-9]{3,95}$";

    /** Server-assigned {@link LegalTag} fields; accepted and discarded on create. */
    private static final Set<String> IGNORED_READ_ONLY_PROPERTIES = Set.of("isValid", "id");

    @NotNull
    @ValidName
    @Schema(description = "The name of the LegalTag", example = "OSDU-Private-EHCData",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = NAME_PATTERN,
            minLength = NAME_MIN_LENGTH,
            maxLength = NAME_MAX_LENGTH)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String name = "";

    @Schema(description = "The description of the LegalTag")
    @XmlJavaTypeAdapter(HtmlEncodeAdapter.class)
    @ValidDescription
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String description = "";

    @Schema(description = "LegalTag properties", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    @JsonDeserialize(using = CreateLegalTagPropertiesDeserializer.class)
    private Properties properties = new Properties();

    @JsonAnySetter
    public void rejectUnknownProperty(String name, Object value) throws UnrecognizedPropertyException {
        if (IGNORED_READ_ONLY_PROPERTIES.contains(name)) {
            return;
        }
        RejectUnknownJsonProperties.reject(this, name);
    }

    public LegalTagDto toLegalTagDto() {
        LegalTagDto dto = new LegalTagDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setProperties(properties);
        return dto;
    }
}
