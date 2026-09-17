package org.opengroup.osdu.legal.swagger;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.acceptancetests.LegalAcceptanceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SwaggerTest extends LegalAcceptanceTests {

    @Test
    public void shouldReturn200_whenSwaggerApiIsCalled() {
        var response = legalTagClient.getSwagger();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertTrue(response.contentType().toLowerCase().contains("text/html"));
    }
}
