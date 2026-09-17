package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;
import org.opengroup.osdu.core.test.client.model.legal.LegalTagsResponse;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class GetLegalTagsApiAcceptanceTests extends LegalAcceptanceTests {

    private String wellKnownName;

    @BeforeEach
    void prepareWellKnownTag() {
        wellKnownName = name;
        assertJsonResponse(legalTagClient.create(createLegalTag("US", wellKnownName)), HttpStatus.SC_CREATED);
    }

    @Test
    public void should_return400Error_when_givingInvalidName() {
        ClientException exception = assertThrows(ClientException.class,
            () -> legalTagClient.batchRetrieve("invalid*name"));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void should_return404_when_givenNonExistingName() {
        ClientException exception = assertThrows(ClientException.class,
            () -> legalTagClient.batchRetrieve(
                servicesConfig.getDataPartitionId() + "-iDoNotExist"));
        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void should_return200_when_userHasApiAccess() {
        assertJsonResponse(legalTagClient.batchRetrieve(wellKnownName), HttpStatus.SC_OK);
    }

    @Test
    public void should_return200onBatchRetrieve_when_userHasApiAccess() {
        String wellKnownName2 = LegalTagUtils.createRandomNameTenant(
            this.getServicesConfig().getDataPartitionId());
        assertJsonResponse(legalTagClient.create(createLegalTag("US", wellKnownName2)), HttpStatus.SC_CREATED);

        HttpResponse<LegalTagsResponse> batchResponse = legalTagClient.batchRetrieve(
            wellKnownName, wellKnownName2);
        assertJsonResponse(batchResponse, HttpStatus.SC_OK);
        LegalTagsResponse legalTags = batchResponse.body();
        assertEquals(2, legalTags.legalTags().length);

        LegalTag legalTag = Arrays.stream(legalTags.legalTags())
            .filter(f -> f.name().equals(wellKnownName)).findFirst().get();
        assertEquals(wellKnownName, legalTag.name());
        assertEquals("A1234", legalTag.properties().contractId());
        assertEquals("US", legalTag.properties().countryOfOrigin().get(0));
        assertEquals(1, legalTag.properties().countryOfOrigin().size());
        assertEquals("Transferred Data", legalTag.properties().dataType());
        assertEquals("EAR99", legalTag.properties().exportClassification());
        assertEquals("MyCompany", legalTag.properties().originator());
        assertEquals("No Personal Data", legalTag.properties().personalData());
        assertEquals("Public", legalTag.properties().securityClassification());

        LegalTag legalTag2 = Arrays.stream(legalTags.legalTags())
            .filter(f -> f.name().equals(wellKnownName2)).findFirst().get();
        assertEquals(wellKnownName2, legalTag2.name());
    }
}
