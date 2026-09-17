package org.opengroup.osdu.legal.tags.validation.rules;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;
import org.springframework.stereotype.Component;

@Component
public class UnownedDataRule extends Rule {

    @Override
    public boolean shouldCheck(Properties properties) {
        return properties.hasThirdPartyDataType() || properties.hasSecondPartyDataType();
    }

    @Override
    protected String hasError(Properties properties) {
        String output ="";
        if(properties.isDefaultExpirationDate() || !properties.hasContractId())
            output = "You need to set an expiration date and contract ID.";
        return output;
    }
}
