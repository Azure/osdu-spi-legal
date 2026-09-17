package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;
import org.opengroup.osdu.core.test.client.model.legal.UpdateLegalTag;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class UpdateLegalTagApiAcceptanceTests extends LegalAcceptanceTests {

    private String expDate;

    @BeforeEach
    void prepareLegalTag() {
        assertJsonResponse(legalTagClient.create(createLegalTag(name)), HttpStatus.SC_CREATED);
        expDate = "2199-12-25";
    }

    @Test
    public void should_returnOk_and_updateProperties_when_userUpdatesExistingLegalTags() {
        HttpResponse<LegalTag> update = legalTagClient.update(new UpdateLegalTag(name, "B1234", null, expDate, null));
        assertJsonResponse(update, HttpStatus.SC_OK);
        LegalTag result = update.body();

        assertEquals("B1234", result.properties().contractId());
        assertEquals("2199-12-25", result.properties().expirationDate());
        assertEquals(name, result.name());
    }

    @Test
    public void should_return400_when_userHasGivenInvalidExpDate()  {
        expDate = "2010-12-31";
        ClientException exception = expectClientError(
            () -> legalTagClient.update(new UpdateLegalTag(name, "B1234", null, expDate, null)));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
        assertEquals(validationError("Expiration date must be a value in the future. Given 2010-12-31"),
            exception.getError());
    }

    @Test
    public void should_return400_when_givenEmptyBody()  {
        ClientException exception = expectClientError(() -> legalTagClient.update(emptyUpdateLegalTag()));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void should_return404_when_givenLegalTagToUpdateThatDoesNotExist() {
        name = LegalTagUtils.createRandomNameTenant(this.getServicesConfig().getDataPartitionId());
        ClientException exception = expectClientError(
            () -> legalTagClient.update(new UpdateLegalTag(name, "B1234", null, expDate, null)));
        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatusCode());
    }
}
