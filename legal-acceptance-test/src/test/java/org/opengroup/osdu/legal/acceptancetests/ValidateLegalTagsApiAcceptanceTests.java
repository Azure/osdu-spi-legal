package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.InvalidTagWithReason;
import org.opengroup.osdu.core.test.client.model.legal.InvalidTagsWithReason;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ValidateLegalTagsApiAcceptanceTests extends LegalAcceptanceTests {

    @Test
    public void should_return400Error_when_givingInvalidName() {
        ClientException exception = expectClientError(() -> legalTagClient.validate("invalid*name"));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void should_return200_withNoInvalidTagsReturned_when_userHasOnlyReadDataAccess() {
        assertJsonResponse(legalTagClient.create(createLegalTag("US", name)), HttpStatus.SC_CREATED);

        HttpResponse<InvalidTagsWithReason> validateResponse = legalTagClient.validate(name);
        assertJsonResponse(validateResponse, HttpStatus.SC_OK);
        assertEquals(0, validateResponse.body().invalidLegalTags().length);
    }

    @Test
    public void should_return200_withNotFoundLegalTagNamesAndReason_when_givenNonexistingLegalTagNames() {
        name = servicesConfig.getDataPartitionId() + "-" + "iDoNotExist";

        HttpResponse<InvalidTagsWithReason> validateResponse = legalTagClient.validate(name);
        assertJsonResponse(validateResponse, HttpStatus.SC_OK);
        InvalidTagsWithReason invalidTagsWithReason = validateResponse.body();
        assertEquals(1, invalidTagsWithReason.invalidLegalTags().length);

        InvalidTagWithReason invalidTagWithReason = invalidTagsWithReason.invalidLegalTags()[0];
        assertEquals(name, invalidTagWithReason.name());
        assertEquals("LegalTag not found", invalidTagWithReason.reason());
    }

    @Disabled
    @Test
    public void should_return200_withLegalTagNamesAndInvalidExpirationDateReason_when_GivenExistingInvalidLegalTagNames() {
        name = "dps-integration-test-1566474656479";

        HttpResponse<InvalidTagsWithReason> validateResponse = legalTagClient.validate(
            servicesConfig.getDataPartitionId() + "-" + name);
        assertJsonResponse(validateResponse, HttpStatus.SC_OK);
        InvalidTagsWithReason invalidTagsWithReason = validateResponse.body();
        assertEquals(1, invalidTagsWithReason.invalidLegalTags().length);

        InvalidTagWithReason invalidTagWithReason = invalidTagsWithReason.invalidLegalTags()[0];
        assertEquals(servicesConfig.getDataPartitionId() + "-" + name, invalidTagWithReason.name());
        assertTrue(invalidTagWithReason.reason().startsWith("Expiration date must be a value in the future."));
    }
}
