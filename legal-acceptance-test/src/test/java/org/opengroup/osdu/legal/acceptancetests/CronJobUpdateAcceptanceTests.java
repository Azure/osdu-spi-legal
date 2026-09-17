package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.InvalidTagsWithReason;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

//TODO: REFACTOR TO USE SECOND TENANT WHEN ADDED TO THE TEST ENVIRONMENT
public final class CronJobUpdateAcceptanceTests extends LegalAcceptanceTests {

    private static final String LEGAL_TAG_DATE = "2099-12-25";
    private static final String LEGAL_TAG_DESC = "description";
    private static final String PRIMARY_CONFIG = "TenantConfigTestingPurpose.json";
    private static final String SECOND_CONFIG = "SecondTenantConfigTestingPurpose.json";

    @Test
    public void should_returnOk_when_runCronJob() throws Exception {
        String primaryPartition = servicesConfig.getDataPartitionId();

        List<String> coo1 = LegalTagUtils.readAlpha2CodesFromResource(PRIMARY_CONFIG);
        List<String> coo2 = LegalTagUtils.readAlpha2CodesFromResource(SECOND_CONFIG);
        Assumptions.assumeFalse(coo1.isEmpty() || coo2.isEmpty());

        String firstCountry = coo1.get(0);
        String secondCountry = coo2.stream()
            .filter(code -> !code.equals(firstCountry))
            .findFirst()
            .orElse(null);
        Assumptions.assumeFalse(firstCountry == null || secondCountry == null);

        long timestamp = System.currentTimeMillis();
        String tagName1 = primaryPartition + "-1-cron-job-test-" + timestamp;
        String tagName2 = primaryPartition + "-2-cron-job-test-" + timestamp;

        legalTagClient.cleanup(tagName1);
        legalTagClient.cleanup(tagName2);

        LegalTag legalTag1 = createCronJobLegalTag(firstCountry, tagName1);
        LegalTag legalTag2 = createCronJobLegalTag(secondCountry, tagName2);

        runUpdateLegalTagStatusJob();
        runUpdateLegalTagStatusJob();

        validateTag(tagName1, firstCountry, legalTag1);
        validateTag(tagName2, secondCountry, legalTag2);
    }

    private LegalTag createCronJobLegalTag(String country, String tagName) {
        LegalTag body = createLegalTag(country, tagName, LEGAL_TAG_DATE, "Third Party Data", LEGAL_TAG_DESC);
        HttpResponse<LegalTag> response = legalTagClient.create(body);
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        return response.body();
    }

    private void validateTag(String tagName, String countryOfOrigin, LegalTag createdTag) {
        HttpResponse<LegalTag> getResponse = legalTagClient.get(tagName);
        assertJsonResponse(getResponse, HttpStatus.SC_OK);
        LegalTag legalTag = getResponse.body();

        assertArrayEquals(new String[] {countryOfOrigin},
            legalTag.properties().countryOfOrigin().toArray(new String[0]));
        assertEquals("MyCompany", legalTag.properties().originator());
        assertEquals("Third Party Data", legalTag.properties().dataType());
        assertEquals("Public", legalTag.properties().securityClassification());
        assertEquals("No Personal Data", legalTag.properties().personalData());
        assertEquals("EAR99", legalTag.properties().exportClassification());

        HttpResponse<InvalidTagsWithReason> validateResponse = legalTagClient.validate(createdTag.name());
        assertJsonResponse(validateResponse, HttpStatus.SC_OK);
        assertEquals(0, validateResponse.body().invalidLegalTags().length);
    }
}
