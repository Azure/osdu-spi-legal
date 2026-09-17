//  Copyright © SLB
//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.acceptanceTests;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.opengroup.osdu.legal.util.Constants;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;

import java.util.Map;

public abstract class CronJobUpdateTests {

    protected LegalTagUtils legalTagUtils;

    private final static String LEGAL_TAG_DATE = "2099-12-25";
    private final static String LEGAL_TAG_TYPE = "Third Party Data";
    private final static String LEGAL_TAG_DESC = "description";

    protected String firstCountry;
    protected String secondCountry;

    @Test
    public void should_returnOk_when_runCronJob() throws Exception {

        String primaryPartition = TestUtils.getMyDataPartition();
        String secondPartition = TestUtils.getMySecondDataPartition();

        Assume.assumeFalse(secondPartition == null || firstCountry == null || secondCountry == null);

        String accessToken = this.legalTagUtils.accessToken();

        Map<String, String> headers = this.legalTagUtils.getHeaders();
        headers.put(Constants.DATA_PARTITION_ID, primaryPartition);
        String tagName = LegalTagUtils.createRandomNameTenant();

        LegalTagUtils.ReadableLegalTag legalTag1Ret = creteLegalTag(firstCountry, tagName, primaryPartition);
        LegalTagUtils.ReadableLegalTag legalTag2Ret = creteLegalTag(secondCountry, tagName, secondPartition);

        runCronJob(headers, accessToken);

        headers.put(Constants.DATA_PARTITION_ID, secondPartition);
        runCronJob(headers, accessToken);

        validateTag(primaryPartition, tagName, accessToken, firstCountry, legalTag1Ret);
        validateTag(secondPartition, tagName, accessToken, secondCountry, legalTag2Ret);

        this.legalTagUtils.delete(tagName, primaryPartition);
        this.legalTagUtils.delete(tagName, secondPartition);
    }

    private LegalTagUtils.ReadableLegalTag creteLegalTag(String country, String tagName, String partition) throws Exception {
        ClientResponse response = this.legalTagUtils.create(country, tagName, LEGAL_TAG_DATE, LEGAL_TAG_TYPE, partition, LEGAL_TAG_DESC);
        Assert.assertEquals(201, response.getStatus());

        return this.legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
    }

    private void runCronJob(Map<String, String> headers, String accessToken) throws Exception {
        ClientResponse response = this.legalTagUtils.send("jobs/updateLegalTagStatus",
                "GET", accessToken, null, "", headers);
        Assert.assertEquals(204, response.getStatus());
    }

    private void validateTag(String partitionId, String tagName, String accessToken, String cco, LegalTagUtils.ReadableLegalTag legalTagRet) throws Exception {
        Map<String, String> headers = this.legalTagUtils.getHeaders();
        headers.put(Constants.DATA_PARTITION_ID, partitionId);
        ClientResponse response = this.legalTagUtils.send("legaltags/" + tagName, "GET", accessToken, null, "", headers);
        LegalTagUtils.ReadableLegalTag legalTag1  = this.legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTag.class);

        String[] countryParam = {cco};
        Assert.assertEquals(legalTag1.properties.countryOfOrigin, countryParam);
        Assert.assertEquals(legalTag1.properties.originator, "MyCompany");
        Assert.assertEquals(legalTag1.properties.dataType, "Third Party Data");
        Assert.assertEquals(legalTag1.properties.securityClassification, "Public");
        Assert.assertEquals(legalTag1.properties.personalData, "No Personal Data");
        Assert.assertEquals(legalTag1.properties.exportClassification, "EAR99");

        // Validate legal tag
        response = this.legalTagUtils.send("legaltags:validate", "POST", accessToken, LegalTagUtils.createRetrieveBatchBody(legalTagRet.name), "", headers);
        LegalTagUtils.InvalidTagsWithReason invalidTagsWithReason = this.legalTagUtils.getResult(response, 200, LegalTagUtils.InvalidTagsWithReason.class);
        Assert.assertEquals(0, invalidTagsWithReason.invalidLegalTags.length);
    }
}
