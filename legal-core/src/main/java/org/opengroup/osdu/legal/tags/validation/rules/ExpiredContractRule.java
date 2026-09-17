package org.opengroup.osdu.legal.tags.validation.rules;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;
import org.springframework.stereotype.Component;

@Component
public class ExpiredContractRule extends Rule {
    @Override
    public boolean shouldCheck(Properties properties) {
        return properties.hasExpired();
    }

    @Override
    protected String hasError(Properties properties) {
        return String.format("Expiration date must be a value in the future. Given %s", properties.getExpirationDate().toLocalDate());
    }
}
