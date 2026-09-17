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
import org.opengroup.osdu.legal.util.AzureLegalTagUtils;

public class TestGetLegalTagApiAcceptance extends GetLegalTagApiAcceptanceTests {

    @Before
    @Override
    public void setup() throws Exception  {
        this.legalTagUtils = new AzureLegalTagUtils();
        super.setup();
    }

    @After
    @Override
    public void teardown() throws Exception  {
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
    
}
