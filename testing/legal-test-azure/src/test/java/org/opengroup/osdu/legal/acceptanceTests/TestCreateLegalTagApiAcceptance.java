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
import org.junit.Before;
import org.junit.Test;
import com.sun.jersey.api.client.ClientResponse;
import org.opengroup.osdu.legal.util.AzureLegalTagUtils;

import static org.opengroup.osdu.legal.util.AzureLegalTagUtils.assertJsonEquals;
import static org.opengroup.osdu.legal.util.AzureLegalTagUtils.validationError;

public class TestCreateLegalTagApiAcceptance extends CreateLegalTagApiAcceptanceTests {

    @Before
    @Override
    public void setup() throws Exception {
        this.legalTagUtils = new AzureLegalTagUtils();
        super.setup();
    }

    @After
    @Override
    public void teardown() throws Exception {
        super.teardown();
        this.legalTagUtils = null;
    }

    @Test
    @Override
	public void should_return307_when_makingHttpRequest()throws Exception{
        //services are enforced to run in https on Azure
	}

    @Test
    @Override
	public void should_return401_when_makingHttpRequestWithoutToken()throws Exception{
        //services are enforced to run in https on Azure
	}

    @Test
    @Override
    public void should_return400_when_userHasApiAccess_ButSendsEmbargoedCOO() throws Exception {
        COO = "SD";
        ClientResponse response = validateAccess(400);
        assertJsonEquals(cooError("SD"), legalTagUtils.getResult(response, 400, String.class));
    }

    @Test
    @Override
    public void should_notAllowCreationOfLegalTag_when_countryOfOriginIsSetToARestrictedCountry() throws Exception {
        COO = "BV";
        ClientResponse response = validateAccess(400);
        assertJsonEquals(cooError("BV"), legalTagUtils.getResult(response, 400, String.class));
    }

    @Test
    @Override
    public void should_return400_CreationOfALegalTag_when_countryOfOriginIsSetToDefaultResidencyRiskCountry_andThen_None_ofThe_Datatype_is_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("MX", name);
        assertJsonEquals(cooError("MX"), legalTagUtils.getResult(response, 400, String.class));
    }

    @Test
    @Override
    public void should_return400_CreationOfALegalTag_when_countryOfOriginIsSetToEmbargoCountry_andThen_input_Datatype_is_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("IR", name, "Transferred Data");
        assertJsonEquals(cooError("IR"), legalTagUtils.getResult(response, 400, String.class));
    }

    private static String cooError(String coo) {
        return validationError("Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: [" + coo + "].");
    }
}
