package org.opengroup.osdu.legal;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;

public class InvalidCooRule extends Rule {
    @Override
    public boolean shouldCheck(Properties properties) {
        return true;
    }

    @Override
    protected String hasError(Properties properties) {return String.format("Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: %s.", properties.getCountryOfOrigin());}
}
