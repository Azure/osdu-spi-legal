package org.opengroup.osdu.legal.util;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.feature.CommonFeatureFlagStateResolverUtil;
import org.opengroup.osdu.core.common.feature.IFeatureFlag;
import org.opengroup.osdu.core.common.model.info.FeatureFlagStateResolver;
import org.opengroup.osdu.core.common.multitenancy.ITenantInfoService;
import org.opengroup.osdu.legal.config.FeatureFlagConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static org.opengroup.osdu.legal.Constants.*;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = FeatureFlagConstants.EXPOSE_FEATUREFLAG_ENABLED_PROPERTY, havingValue = "true", matchIfMissing = true)
public class FeatureFlagStateConfiguration {

  private final ITenantInfoService tenantInfoService;
  private final IFeatureFlag featureFlagService;

  @Bean
  @RequestScope
  public FeatureFlagStateResolver aboutToExpireFFResolver() {
    return CommonFeatureFlagStateResolverUtil.buildCommonFFStateResolver(
        ABOUT_TO_EXPIRE_FEATURE_NAME,
        tenantInfoService,
        featureFlagService
    );
  }

  @Bean
  @RequestScope
  public FeatureFlagStateResolver legalQueryApiFFResolver() {
    return CommonFeatureFlagStateResolverUtil.buildCommonFFStateResolver(
        LEGAL_QUERY_API_FEATURE_NAME,
        tenantInfoService,
        featureFlagService
    );
  }

  @Bean
  @RequestScope
  public FeatureFlagStateResolver legalQueryApiFreeTextAllFieldsFFResolver() {
    return CommonFeatureFlagStateResolverUtil.buildCommonFFStateResolver(
        LEGAL_QUERY_API_FREE_TEXT_ALL_FIELDS_FEATURE_NAME,
        tenantInfoService,
        featureFlagService
    );
  }

}
