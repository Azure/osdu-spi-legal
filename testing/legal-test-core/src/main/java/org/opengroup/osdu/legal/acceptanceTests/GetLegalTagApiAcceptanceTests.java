package org.opengroup.osdu.legal.acceptanceTests;

import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public abstract class GetLegalTagApiAcceptanceTests extends AcceptanceBaseTest {

    protected String name;

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
    public void should_returnDefaultExpirationDate_when_expirationDateIsNotProvided() throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        ClientResponse resp = legalTagUtils.create("US", name, "", "Transferred Data", TestUtils.getMyDataPartition(), null);
        LegalTagUtils.ReadableLegalTag result = legalTagUtils.getResult(resp, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals("9999-12-31", result.properties.expirationDate);
        legalTagUtils.delete(name);
    }

    @Test
    public void should_beAbleToRetrieveLegalTag_when_tenantIsAutomaticallyAppendedOnCreation() throws Exception {
        name = LegalTagUtils.createRandomNameTenant();
        ClientResponse response = legalTagUtils.create("US", name, "2099-12-25", "Third Party Data");
        legalTagUtils.getResult(response, 201, String.class);

        Map<String, String> headers = legalTagUtils.getHeaders();
        response = legalTagUtils.send("legaltags/" + name, "GET",
                legalTagUtils.accessToken(), "", "", headers);
        legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTag.class);
        legalTagUtils.delete(name);
    }

    @Test
    public void should_beAbleToRetrieveLegalTag_when_tenantIsAutomaticallyAppendedOnCreation_withTrailingSlash() throws Exception {
        name = LegalTagUtils.createRandomNameTenant();
        ClientResponse response = legalTagUtils.create("US", name, "2099-12-25", "Third Party Data");
        legalTagUtils.getResult(response, 201, String.class);

        Map<String, String> headers = legalTagUtils.getHeaders();
        response = legalTagUtils.send("legaltags/" + name + "/", "GET",
                legalTagUtils.accessToken(), "", "", headers);
        legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTag.class);
        legalTagUtils.delete(name);
    }

    @Test
    public void should_retrieveLegalTag_when_givenExistingName() throws Exception {
        name = LegalTagUtils.createRandomNameTenant();
        ClientResponse result = legalTagUtils.create("US", name);
        legalTagUtils.getResult(result, 201, LegalTagUtils.ReadableLegalTag.class);

        ClientResponse response = send("", 200, "", TestUtils.getMyDataPartition());
        LegalTagUtils.ReadableLegalTag legalTag = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTag.class);

        assertEquals(name, legalTag.name);
        assertEquals("<my description>", legalTag.description);

        assertEquals("A1234", legalTag.properties.contractId);
        assertEquals("US", legalTag.properties.countryOfOrigin[0]);
        assertEquals("Transferred Data", legalTag.properties.dataType);
        assertEquals("EAR99", legalTag.properties.exportClassification);
        assertEquals("2099-12-25", legalTag.properties.expirationDate);
        assertEquals("MyCompany", legalTag.properties.originator);
        assertEquals("No Personal Data", legalTag.properties.personalData);
        assertEquals("Public", legalTag.properties.securityClassification);

        legalTagUtils.delete(name);
    }

    @Test
    @Override
    public void should_return401_when_makingHttpRequestWithoutToken()throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        super.should_return401_when_makingHttpRequestWithoutToken();
    }

    @Override
    protected String getBody(){
        return "";
    }
    @Override
    protected String getApi() {
        return "legaltags/" + name;
    }
    @Override
    protected String getHttpMethod() {
        return "GET";
    }
}
