package org.opengroup.osdu.legal.acceptancetests;

import org.opengroup.osdu.core.test.auth.UserType;
import org.opengroup.osdu.core.test.base.BaseGetInfoAcceptanceTests;
import org.opengroup.osdu.core.test.service.ServiceType;

import java.util.List;

public final class GetInfoApiAcceptanceTests extends BaseGetInfoAcceptanceTests {

  // Expected feature flags for legal service
  private static final List<String> expectedFeatureFlags = List.of(
      "featureFlag.aboutToExpireLegalTag.enabled",
      "featureFlag.legalTagQueryApi.enabled",
      "featureFlag.legalTagQueryApiFreeTextAllFields.enabled"
  );

  public GetInfoApiAcceptanceTests() {
    super(UserType.PRIVILEGED_USER, ServiceType.LEGAL_V1, expectedFeatureFlags);
  }

}
