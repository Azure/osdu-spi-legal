package org.opengroup.osdu.legal;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;

public class MockRule extends Rule {
    @Override
    public boolean shouldCheck(Properties properties) {
        return true;
    }

    @Override
    protected String hasError(Properties properties) {return "";}
}