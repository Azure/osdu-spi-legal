package org.opengroup.osdu.legal.acceptanceTests;

import org.junit.*;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;
import com.sun.jersey.api.client.ClientResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.opengroup.osdu.legal.util.Constants.DATA_PARTITION_ID;

public abstract class UpdateLegalTagApiAcceptanceTests extends AcceptanceBaseTest {

    static protected String defaultName = LegalTagUtils.createRandomNameTenant();
    protected String name;
    protected String expDate;

    @Before
    @Override
    public void setup() throws Exception {
        legalTagUtils.create(defaultName);
        name = defaultName;
        expDate = "2199-12-25";
    }

    @After
    @Override
    public void teardown() throws Exception{
        legalTagUtils.delete(defaultName);
    }

    @Test
    public void should_returnOk_and_updateProperties_when_userUpdatesExistingLegalTags() throws Exception{
        Map<String, String> headers = new HashMap<>();
        headers.put(DATA_PARTITION_ID, TestUtils.getMyDataPartition());
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(), getQuery(), headers);
        LegalTagUtils.ReadableLegalTag result = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTag.class);

        assertEquals("B1234", result.properties.contractId);
        assertEquals("2199-12-25", result.properties.expirationDate);
        assertEquals(name, result.name);
    }
    @Test
    public void should_return400_when_userHasGivenInvalidExpDate() throws Exception{
        expDate = "2010-12-31"; //set expired date
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(), getQuery());
        String error = legalTagUtils.getResult(response, 400, String.class);
        assertEquals("{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"{\\\"errors\\\":[\\\"Expiration date must be a value in the future. Given 2010-12-31\\\"]}\"}", error);
    }

    @Test
    public void should_return400_when_givenEmptyBody() throws Exception{
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), "{}", getQuery());
        assertEquals(400, response.getStatus());
    }

    @Test
    public void should_return404_when_givenLegalTagToUpdateThatDoesNotExist() throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(), getQuery());
        assertEquals(404, response.getStatus());
    }

    @Override
    protected String getBody(){
        return LegalTagUtils.updateBody(name, expDate);
    }

    @Override
    protected String getApi() {
        return "legaltags";
    }

    @Override
    protected String getHttpMethod() {
        return "PUT";
    }
}
