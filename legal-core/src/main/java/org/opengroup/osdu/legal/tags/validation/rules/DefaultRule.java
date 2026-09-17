package org.opengroup.osdu.legal.tags.validation.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.springframework.stereotype.Component;

import java.util.Map;

import jakarta.inject.Inject;


//This should be the last rule used as a catch all when specific rules have not been found to handle validation
@Component
public class DefaultRule extends Rule {
    @Inject
    private LegalTagCountriesService legalTagCountriesService;

    @Override
    public boolean shouldCheck(Properties properties) {
        return true;
    }

    @Override
    protected String hasError(Properties properties) {
        String output = "";
        try {
            if(!properties.hasContractId() && !properties.isUnknownOrNonExistantContractId()) {
                output = String.format("Invalid Contract Id given. Only '%s' '%s' or the real contract ID are allowed values. The contract ID must be between 3 and 40 characters and only include alphanumeric values and hyphens,", Properties.UNKNOWN_CONTRACT_ID, Properties.NO_CONTRACT_ID);
            }
            else if(!isAllowedCOO(properties)){
                output = String.format("Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: %s.", properties.getCountryOfOrigin());
            }
        } catch (AppException ex) {
            output = ex.getError().getMessage();
        }
        return output;
    }

    @JsonIgnore
    private boolean isAllowedCOO(Properties properties){
        Map<String, String> validCOOs = legalTagCountriesService.getValidCOOs(properties.getDataType());
        return properties.getCountryOfOrigin() != null && !properties.getCountryOfOrigin().isEmpty() &&
                properties.getCountryOfOrigin().stream().allMatch(validCOOs::containsKey);
    }
}
