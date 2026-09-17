package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.test.auth.UserType;
import org.opengroup.osdu.core.test.base.BaseAcceptanceTests;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.LegalTagsClient;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;
import org.opengroup.osdu.core.test.client.model.legal.LegalTagProperties;
import org.opengroup.osdu.core.test.client.model.legal.UpdateLegalTag;
import org.opengroup.osdu.core.test.service.ServiceType;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class LegalAcceptanceTests extends BaseAcceptanceTests {

  protected static final String DEFAULT_EXPIRATION_DATE = "2099-12-25";
  protected static final String DEFAULT_DESCRIPTION = "Acceptance test legal tag";

  protected String name;
  protected final LegalTagsClient legalTagClient;

  public LegalAcceptanceTests() {
    super(List.of(UserType.PRIVILEGED_USER),
        List.of(ServiceType.LEGAL_V1));
    legalTagClient = new LegalTagsClient(stringHttpClient, getDefaultUser());
  }

  @BeforeEach
  @Override
  protected void setup() {
    name = LegalTagUtils.createRandomNameTenant(getServicesConfig().getDataPartitionId());
  }

  @AfterEach
  @Override
  public void teardown() {
    legalTagClient.teardown();
  }

  protected static void assertJsonResponse(HttpResponse<?> response, int expectedStatus) {
    assertEquals(expectedStatus, response.statusCode());
    assertTrue(response.contentType().toLowerCase().contains("application/json"));
  }

  protected ClientException expectClientError(Executable action) {
    return assertThrows(ClientException.class, action);
  }

  protected static AppError validationError(String validationMessage) {
    return new AppError(HttpStatus.SC_BAD_REQUEST, "Validation error.",
        "{\"errors\":[\"" + validationMessage + "\"]}");
  }

  protected void runUpdateLegalTagStatusJob() {
    assertEquals(HttpStatus.SC_NO_CONTENT, legalTagClient.runUpdateLegalTagStatusJob().statusCode());
  }

  protected static LegalTag emptyLegalTag() {
    return new LegalTag(null, null, null, null, null);
  }

  protected static UpdateLegalTag emptyUpdateLegalTag() {
    return new UpdateLegalTag(null, null, null, null, null);
  }

  protected static LegalTag createLegalTag(String name) {
    return createLegalTag("US", name);
  }

  protected static LegalTag createLegalTag(String countryOfOrigin, String name) {
    return createLegalTag(countryOfOrigin, name, DEFAULT_EXPIRATION_DATE, "Transferred Data",
        DEFAULT_DESCRIPTION);
  }

  protected static LegalTag createLegalTag(String countryOfOrigin, String name, String dataType) {
    return createLegalTag(countryOfOrigin, name, DEFAULT_EXPIRATION_DATE, dataType, DEFAULT_DESCRIPTION);
  }

  protected static LegalTag createLegalTag(String countryOfOrigin, String name, String expDate,
                                           String dataType) {
    return createLegalTag(countryOfOrigin, name, expDate, dataType, DEFAULT_DESCRIPTION);
  }

  protected static LegalTag createLegalTag(String countryOfOrigin, String name, String expDate,
                                           String dataType, String description) {
    return new LegalTag(null, null, name,
        new LegalTagProperties("A1234", "MyCompany", List.of(countryOfOrigin), "Public", "EAR99",
            "No Personal Data", expDate, dataType),
        description);
  }

}
