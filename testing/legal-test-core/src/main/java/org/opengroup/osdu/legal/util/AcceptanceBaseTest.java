package org.opengroup.osdu.legal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opengroup.osdu.legal.util.Constants.DATA_PARTITION_ID;

import com.sun.jersey.api.client.ClientResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.java.Log;
import org.junit.Test;

@Log
public abstract class AcceptanceBaseTest {

	protected LegalTagUtils legalTagUtils;

	protected abstract String getApi();

	protected abstract String getHttpMethod();

	public void setup() throws Exception {}

	public void teardown() throws Exception {}

	protected String getBody(){
		return "{}";
	}

	protected String getQuery(){
		return "";
	}

	@Test
	public void should_returnOk_when_makingHttpOptionsRequest() throws Exception{
		ClientResponse response = legalTagUtils.send(this.getApi(), "OPTIONS", legalTagUtils.accessToken(), "", "");
		assertEquals(200, response.getStatus());
	}

	@Test
	public void should_return307_when_makingHttpRequest()throws Exception{
		if(this.legalTagUtils.isLocalHost() || legalTagUtils.skipHttp())
			return; //jetty server returns 403 when running locally when deployed jettyserver is not used and the app returns a 302 so just run against deployed version only when checking http -> https redirects
		TestUtils testUtils = new TestUtils(true);
		ClientResponse response = testUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(),
				this.getBody(), this.getQuery());
		assertEquals(307, response.getStatus());
	}

	@Test
	public void should_return401_when_makingHttpRequestWithoutToken()throws Exception{
		if(this.legalTagUtils.isLocalHost() || legalTagUtils.skipHttp())
			return; //jetty server returns 403 when running locally when deployed jettyserver is not used and the app returns a 302 so just run against deployed version only when checking http -> https redirects

		TestUtils testUtils = new TestUtils(true);
		ClientResponse response = testUtils.send(this.getApi(), this.getHttpMethod(), "",
				this.getBody(), this.getQuery());
		assertEquals(401, response.getStatus());
	}

	protected ClientResponse send(String requestBody, int responseStatus) throws Exception {

		return this.send(requestBody, responseStatus, "");
	}

	protected ClientResponse send(String requestBody, int responseStatus, String query) throws Exception {
		return send(requestBody, responseStatus, query, LegalTagUtils.getMyDataPartition());
	}

	protected ClientResponse send(String requestBody, int responseStatus, String query, String tenant) throws Exception {
		Map<String, String> headers = legalTagUtils.getHeaders();
		headers.put(DATA_PARTITION_ID, tenant);
		ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), requestBody,
				query, headers);

		assertEquals(responseStatus, response.getStatus());

		if (responseStatus == 204) //no content
		{
			return null;
		}

		return response;
	}

	protected ClientResponse send(String requestBody, String query, String tenant) throws Exception {
		Map<String, String> headers = legalTagUtils.getHeaders();
		headers.put(DATA_PARTITION_ID, tenant);
		return legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), requestBody,
				query, headers);
	}

  protected ClientResponse validateAccess(int expectedResponse) throws Exception {
    Map<String, String> headers = new HashMap<>();
    headers.put(DATA_PARTITION_ID, LegalTagUtils.getMyDataPartition());

    ClientResponse response = legalTagUtils
        .send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(),
            getQuery(), headers);
    log.info("Response status = " + response.getStatus());
    assertEquals(expectedResponse, response.getStatus());
    if (expectedResponse == 204) {
      if (Objects.nonNull(response.getType())) {
        log.info("Content-Type = " + response.getType().toString());
        assertTrue(response.getType().toString().toLowerCase().indexOf("text/html") >= 0); //Google Cloud Run specific
      } else {
        assertNull(response.getType());
      }
    } else if (response.getType() != null) {
      assertTrue(response.getType().toString().toLowerCase().indexOf("application/json") >= 0);
    }
    return response;
  }
}