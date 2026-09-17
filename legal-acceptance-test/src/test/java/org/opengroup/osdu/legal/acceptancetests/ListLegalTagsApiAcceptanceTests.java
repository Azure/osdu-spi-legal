package org.opengroup.osdu.legal.acceptancetests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;
import org.opengroup.osdu.core.test.client.model.legal.LegalTagsResponse;

import com.google.common.base.Strings;

public final class ListLegalTagsApiAcceptanceTests extends LegalAcceptanceTests {

    @BeforeEach
    @Override
    protected void setup() {
        super.setup();
        assertJsonResponse(legalTagClient.create(createLegalTag("US", name)), HttpStatus.SC_CREATED);
    }

    @Test
    public void should_return200_and_allValidLegalTags_when_sendingValidTrueParameter_And_notSendingValidParameter() {
        HttpResponse<LegalTagsResponse> response = legalTagClient.list(true);
        assertJsonResponse(response, HttpStatus.SC_OK);
        LegalTagsResponse result = response.body();
        assertTrue(result.legalTags().length > 0);
        assertFalse(Strings.isNullOrEmpty(result.legalTags()[0].name()));
        assertFalse(Strings.isNullOrEmpty(result.legalTags()[0].properties().countryOfOrigin().get(0)));

        HttpResponse<LegalTagsResponse> response2 = legalTagClient.list();
        assertJsonResponse(response2, HttpStatus.SC_OK);
        LegalTagsResponse result2 = response2.body();
        for (LegalTag tag : result.legalTags()) {
            assertTrue(Arrays.stream(result2.legalTags()).anyMatch(s -> tag.name().equals(s.name())));
        }
    }

    @Test
    public void should_returnDifferentResults_when_sendingValidParameterTrueOrFalse() {
        HttpResponse<LegalTagsResponse> response = legalTagClient.list(true);
        assertJsonResponse(response, HttpStatus.SC_OK);
        LegalTagsResponse result = response.body();

        HttpResponse<LegalTagsResponse> response2 = legalTagClient.list(false);
        assertJsonResponse(response2, HttpStatus.SC_OK);
        LegalTagsResponse result2 = response2.body();

        for (LegalTag tag : result.legalTags()) {
            assertFalse(Arrays.stream(result2.legalTags()).anyMatch(s -> tag.name().equals(s.name())));
        }
    }
}
