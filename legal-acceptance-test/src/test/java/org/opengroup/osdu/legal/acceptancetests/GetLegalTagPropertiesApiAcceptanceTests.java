package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.LegalTagPropertyValues;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GetLegalTagPropertiesApiAcceptanceTests extends LegalAcceptanceTests {

    @Test
    public void should_returnAllLegalTagProperties_when_getPropertiesApi() {
        HttpResponse<LegalTagPropertyValues> response = legalTagClient.getProperties();
        assertJsonResponse(response, HttpStatus.SC_OK);
        LegalTagPropertyValues result = response.body();

        assertFalse(result.countriesOfOrigin().isEmpty());
        assertTrue(result.countriesOfOrigin().containsKey("US"));

        assertFalse(result.otherRelevantDataCountries().isEmpty());
        assertTrue(result.otherRelevantDataCountries().containsKey("FR"));

        assertFalse(result.personalDataTypes().isEmpty());
        assertTrue(result.personalDataTypes().contains("No Personal Data"));

        assertFalse(result.securityClassifications().isEmpty());
        assertTrue(result.securityClassifications().contains("Private"));

        assertFalse(result.exportClassificationControlNumbers().isEmpty());
        assertTrue(result.exportClassificationControlNumbers().contains("EAR99"));

        assertFalse(result.dataTypes().isEmpty());
        assertTrue(result.dataTypes().contains("First Party Data"));
    }
}
