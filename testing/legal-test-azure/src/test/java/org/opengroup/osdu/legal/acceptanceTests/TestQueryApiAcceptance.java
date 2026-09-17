package org.opengroup.osdu.legal.acceptanceTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.legal.util.AzureLegalTagUtils;

public class TestQueryApiAcceptance extends QueryLegalTagsApiAcceptanceTests {

  @Before
  @Override
  public void setup() throws Exception {
    this.legalTagUtils = new AzureLegalTagUtils();
    super.setup();
  }

  @After
  @Override
  public void teardown() throws Exception {
    super.teardown();
    this.legalTagUtils = null;
  }

  @Test
  @Override
  public void should_return307_when_makingHttpRequest() throws Exception {
    // services are enforced to run in https on Azure
  }

  @Test
  @Override
  public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
    // services are enforced to run in https on Azure
  }
}