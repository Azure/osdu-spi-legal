package org.opengroup.osdu.legal;

import org.opengroup.osdu.core.common.feature.IFeatureFlag;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class FeatureFlagController {

    @Autowired
    private IFeatureFlag aboutToExpireLegalTagFeatureFlag;    

    @Autowired
    private IFeatureFlag legalTagQueryApiFeatureFlag;    

    @Autowired
    private IFeatureFlag legalTagQueryApiFreeTextAllFieldsFeatureFlag;    

    public Boolean isAboutToExpireFeatureFlagEnabled() {
        return aboutToExpireLegalTagFeatureFlag.isFeatureEnabled(Constants.ABOUT_TO_EXPIRE_FEATURE_NAME);
    }

    public Boolean isLegalTagQueryApiFlagEnabled() {
        return legalTagQueryApiFeatureFlag.isFeatureEnabled(Constants.LEGAL_QUERY_API_FEATURE_NAME);
    }

    public Boolean isLegalTagQueryApiFreeTextAllFieldsFlagEnabled() {
        return legalTagQueryApiFreeTextAllFieldsFeatureFlag.isFeatureEnabled(Constants.LEGAL_QUERY_API_FREE_TEXT_ALL_FIELDS_FEATURE_NAME);
    }
}
