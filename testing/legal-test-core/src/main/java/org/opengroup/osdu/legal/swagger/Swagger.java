package org.opengroup.osdu.legal.swagger;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;

import static org.junit.Assert.assertEquals;

public abstract class Swagger extends AcceptanceBaseTest {

    protected static final String SWAGGER_API_PATH = "swagger";

    @Test
    public void shouldReturn200_whenSwaggerApiIsCalled() throws Exception {
        ClientResponse response = legalTagUtils.send(SWAGGER_API_PATH, "GET", "", "", "");
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    @Override
    public void should_returnOk_when_makingHttpOptionsRequest() {
        // not actual for this endpoint
    }

    @Test
    public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
        // not actual for this endpoint
    }

    @Test
    public void should_return307_when_makingHttpRequest()throws Exception {
        // not actual for this endpoint
    }

    @Override
    protected String getApi() {
        return SWAGGER_API_PATH;
    }

    @Override
    protected String getHttpMethod() {
        return "GET";
    }

}
