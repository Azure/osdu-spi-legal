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

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.legal.util.AzureLegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;

import java.util.List;


public class TestCronJobUpdate extends CronJobUpdateTests {

    private static final String storageAccount1 = System.getProperty("AZURE_LEGAL_STORAGE_ACCOUNT", System.getenv("AZURE_LEGAL_STORAGE_ACCOUNT"));
    private static final String storageAccount2 = System.getProperty("AZURE_LEGAL_STORAGE_ACCOUNT_SECOND", System.getenv("AZURE_LEGAL_STORAGE_ACCOUNT_SECOND"));

    private static final String defaultCOOJSONFileName1 = "TenantConfigTestingPurpose.json";
    private static final String defaultCOOJSONFileName2 = "SecondTenantConfigTestingPurpose.json";

    @Before
    public void setup() throws Exception {
        this.legalTagUtils = new AzureLegalTagUtils();
    }

    @After
    public void teardown() throws Exception {
        this.legalTagUtils = null;
    }

    @Test
    public void should_returnOk_when_runCronJob() throws Exception {

        String secondPartition = TestUtils.getMySecondDataPartition();
        Assume.assumeFalse(secondPartition == null || storageAccount1 == null || storageAccount2 == null);

        List<String> coo1 = legalTagUtils.readCOOCountries(storageAccount1, defaultCOOJSONFileName1);
        List<String> coo2 = legalTagUtils.readCOOCountries(storageAccount2, defaultCOOJSONFileName2);

        Assume.assumeTrue(coo1 != null && !coo1.isEmpty());
        this.firstCountry = coo1.get(0);
        Assume.assumeFalse(firstCountry == null);
        this.secondCountry = coo2.stream().filter(e -> !e.equals(coo1.get(0))).findFirst().orElse(null);
        Assume.assumeFalse(secondCountry == null);

        super.should_returnOk_when_runCronJob();
    }
}
