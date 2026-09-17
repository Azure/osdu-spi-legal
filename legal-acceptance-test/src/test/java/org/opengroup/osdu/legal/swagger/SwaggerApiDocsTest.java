package org.opengroup.osdu.legal.swagger;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.acceptancetests.LegalAcceptanceTests;

import static com.nimbusds.oauth2.sdk.http.HTTPResponse.SC_OK;

public final class SwaggerApiDocsTest extends LegalAcceptanceTests {
    @Test
    public void shouldReturn200_whenSwaggerApiDocsIsCalled() {
        var response = legalTagClient.getApiDocs();
        assertJsonResponse(response, SC_OK);
    }
}
