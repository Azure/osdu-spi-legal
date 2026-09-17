package org.opengroup.osdu.legal;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.tags.validation.rules.DefaultRule;
import org.opengroup.osdu.legal.tags.validation.rules.ExpiredContractRule;
import org.opengroup.osdu.legal.tags.validation.rules.UnownedDataRule;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.ArrayList;
import java.util.List;

@Component
public class RulesetProvider implements Provider<List<Rule>> {

    @Inject
    private ExpiredContractRule expiredContractRule;

    @Inject
    private UnownedDataRule unownedDataRule;

    @Inject
    private DefaultRule defaultRule;

    @Inject
    private RequestInfo requestInfo;

    @Override
    public List<Rule> get() {
        List<Rule> ruleset = new ArrayList<>();

        TenantInfo tenantInfo = requestInfo.getTenantInfo();
        if (tenantInfo == null)
            throw new RuntimeException("No tenant info");

        switch(tenantInfo.getComplianceRuleSet()) {
            case TenantInfo.ComplianceRuleSets.SHARED:
                ruleset.add(expiredContractRule);
                ruleset.add(unownedDataRule);
                ruleset.add(defaultRule);
                return ruleset;
            default:
                throw new AppException(500, "Server error", "Unexpected ruleset requested " + requestInfo.getTenantInfo().getComplianceRuleSet());
        }
    }
}
