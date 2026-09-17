package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class DeleteLegalTagApiAcceptanceTests extends LegalAcceptanceTests {

    @Test
    public void should_return204_when_deletingAContractThatDoesNotExist() {
        assertEquals(HttpStatus.SC_NO_CONTENT, legalTagClient.delete(name).statusCode());
    }

    @Test
    public void should_return204_when_deletingAContractThatDoesExist() {
        HttpResponse<LegalTag> createResponse = legalTagClient.create(createLegalTag(name));
        assertJsonResponse(createResponse, HttpStatus.SC_CREATED);
        String createdName = createResponse.body().name();
        assertEquals(HttpStatus.SC_NO_CONTENT, legalTagClient.delete(createdName).statusCode());
    }

    @Test
    public void should_return400_when_deletingAContractWithAnInvalidName() {
        ClientException exception = assertThrows(ClientException.class,
            () -> legalTagClient.delete("invalid*name"));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
    }
}
