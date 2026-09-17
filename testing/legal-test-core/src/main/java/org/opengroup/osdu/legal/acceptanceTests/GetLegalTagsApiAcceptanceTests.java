package org.opengroup.osdu.legal.acceptanceTests;

import org.junit.*;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;
import com.sun.jersey.api.client.ClientResponse;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public abstract class GetLegalTagsApiAcceptanceTests extends AcceptanceBaseTest {

    protected String name;

    static protected String wellKnownName = LegalTagUtils.getMyDataPartition() + "-" + LegalTagUtils.createRandomNameTenant();

    @Before
    @Override
    public void setup() throws Exception {
        ClientResponse response = legalTagUtils.create("US", wellKnownName);
        legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
    }

    @After
    public void teardown() throws Exception {
        legalTagUtils.delete(wellKnownName);
    }

    @Test
    public void should_return400Error_when_givingInvalidName()throws Exception{
        name = "invalid*name";
        validateAccess(400);
    }

    @Test
    public void should_return404_when_givenNonExistingName()throws Exception{
        name = TestUtils.getMyDataPartition() + "-iDoNotExist";
        validateAccess(404);
    }

    @Test
    public void should_return200_when_userHasApiAccess() throws Exception{
        name = wellKnownName;
        validateAccess(200);
    }

    @Test
    public void should_return200onBatchRetrieve_when_userHasApiAccess() throws Exception {
        String wellKnownName2 = LegalTagUtils.getMyDataPartition() + "-" + LegalTagUtils.createRandomNameTenant();
        ClientResponse response = legalTagUtils.create("US", wellKnownName2);
        legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);

        response = legalTagUtils.send("legaltags:batchRetrieve", "POST", legalTagUtils.accessToken(),
                LegalTagUtils.createRetrieveBatchBody(wellKnownName, wellKnownName2), "");

        LegalTagUtils.ReadableLegalTags legalTags = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTags.class);
        assertEquals(2, legalTags.legalTags.length);

        LegalTagUtils.ReadableLegalTag legalTag = Arrays.stream(legalTags.legalTags).filter(f -> f.name.equals(wellKnownName)).findFirst().get();
        assertEquals(wellKnownName, legalTag.name);
        assertEquals("A1234", legalTag.properties.contractId);
        assertEquals("US", legalTag.properties.countryOfOrigin[0]);
        assertEquals(1, legalTag.properties.countryOfOrigin.length);
        assertEquals("Transferred Data", legalTag.properties.dataType);
        assertEquals("EAR99", legalTag.properties.exportClassification);
        assertEquals("MyCompany", legalTag.properties.originator);
        assertEquals("No Personal Data", legalTag.properties.personalData);
        assertEquals("Public", legalTag.properties.securityClassification);

        LegalTagUtils.ReadableLegalTag legalTag2 = Arrays.stream(legalTags.legalTags).filter(f -> f.name.equals(wellKnownName2)).findFirst().get();
        assertEquals(wellKnownName2, legalTag2.name);
        legalTagUtils.delete(wellKnownName2);
    }

    @Test
    @Override
    public void should_return401_when_makingHttpRequestWithoutToken()throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        super.should_return401_when_makingHttpRequestWithoutToken();
    }

    @Override
    protected String getBody(){
        return LegalTagUtils.createRetrieveBatchBody(name);
    }
    @Override
    protected String getApi() {
        return "legaltags:batchRetrieve";
    }

    @Override
    protected String getHttpMethod() {
        return "POST";
    }
}
