package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class GetLegalTagApiAcceptanceTests extends LegalAcceptanceTests {

    @Test
    public void should_return400Error_when_givingInvalidName() {
        ClientException exception = assertThrows(ClientException.class,
            () -> legalTagClient.get("invalid*name"));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void should_return404_when_givenNonExistingName() {
        ClientException exception = assertThrows(ClientException.class,
            () -> legalTagClient.get(servicesConfig.getDataPartitionId() + "-iDoNotExist"));
        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void should_returnDefaultExpirationDate_when_expirationDateIsNotProvided() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("US", name, null, "Transferred Data", null));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        assertEquals("9999-12-31", response.body().properties().expirationDate());
    }

    @Test
    public void should_beAbleToRetrieveLegalTag_when_tenantIsAutomaticallyAppendedOnCreation() {
        HttpResponse<LegalTag> createResponse = legalTagClient.create(
            createLegalTag("US", name, "2099-12-25", "Third Party Data"));
        assertJsonResponse(createResponse, HttpStatus.SC_CREATED);
        assertJsonResponse(legalTagClient.get(name), HttpStatus.SC_OK);
    }

    @Test
    public void should_beAbleToRetrieveLegalTag_when_tenantIsAutomaticallyAppendedOnCreation_withTrailingSlash() {
        HttpResponse<LegalTag> createResponse = legalTagClient.create(
            createLegalTag("US", name, "2099-12-25", "Third Party Data"));
        assertJsonResponse(createResponse, HttpStatus.SC_CREATED);
        assertJsonResponse(legalTagClient.get(name), HttpStatus.SC_OK);
    }

    @Test
    public void should_retrieveLegalTag_when_givenExistingName() {
        assertJsonResponse(legalTagClient.create(createLegalTag("US", name)), HttpStatus.SC_CREATED);
        HttpResponse<LegalTag> getResponse = legalTagClient.get(name);
        assertJsonResponse(getResponse, HttpStatus.SC_OK);
        LegalTag legalTag = getResponse.body();

        assertEquals(name, legalTag.name());
        assertEquals("Acceptance test legal tag", legalTag.description());

        assertEquals("A1234", legalTag.properties().contractId());
        assertEquals("US", legalTag.properties().countryOfOrigin().get(0));
        assertEquals("Transferred Data", legalTag.properties().dataType());
        assertEquals("EAR99", legalTag.properties().exportClassification());
        assertEquals("2099-12-25", legalTag.properties().expirationDate());
        assertEquals("MyCompany", legalTag.properties().originator());
        assertEquals("No Personal Data", legalTag.properties().personalData());
        assertEquals("Public", legalTag.properties().securityClassification());
    }
}
