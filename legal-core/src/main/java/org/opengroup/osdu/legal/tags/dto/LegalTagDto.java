package org.opengroup.osdu.legal.tags.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.core.common.model.legal.CountryCodes;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.ValidDescription;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import org.opengroup.osdu.legal.util.HtmlEncodeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/*
 * Read only version of LegalTag model that are sent to the user
 *
 * If any class variable changed here,
 * need to update the corresponding doc model class in SwaggerHelper.java
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a single LegalTag")
public class LegalTagDto {

    @Schema(description = "The name of the LegalTag", example = "OSDU-Private-EHCData")
    @ValidName
    private String name = "";

    @Schema(description = "The description of the LegalTag")
    @XmlJavaTypeAdapter(HtmlEncodeAdapter.class)
    @ValidDescription
    private String description = "";

    @Schema(description = "LegalTag properties")
    @Valid
    private Properties properties = new Properties();

    public static LegalTagDto convertTo(LegalTag legalTag){
        if(legalTag == null)
            return null;
        LegalTagDto output = new LegalTagDto();
        output.setName(legalTag.getName());
        output.setProperties(legalTag.getProperties());

        String tempDescription = legalTag.getDescription();
        for(String alpha2 : output.properties.getCountryOfOrigin()) {
            CountryCodes code = CountryCodes.getByCode(alpha2);
            if(code.needsClientConsent()){
                tempDescription =
                        String.format("One or more Country of Origin for these Legal tag require consent of client or a license. Please make sure the data is in compliance. %s",
                            tempDescription);
                break;
            }
        }
        output.setDescription(tempDescription);

        return output;
    }
    public static LegalTag convertFrom(LegalTagDto legalTagDto){
        if(legalTagDto == null)
            return null;

        LegalTag output = new LegalTag();
        output.setName(legalTagDto.getName());
        output.setDescription(legalTagDto.getDescription());
        output.setProperties(legalTagDto.getProperties());
        output.setDefaultId();
        return output;
    }
}
