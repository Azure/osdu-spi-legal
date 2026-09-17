package org.opengroup.osdu.legal.acceptanceTests;

import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class CreateLegalTagApiAcceptanceTests extends AcceptanceBaseTest {

    protected String name = LegalTagUtils.createRandomNameTenant();
    protected String COO = "";

    @Before
    @Override
    public void setup() throws Exception {
        COO = "US";
    }

    @After
    @Override
    public void teardown() throws Exception {
        legalTagUtils.delete(name);
    }

    @Test
    public void should_returnCreated_when_userHasApiAccess() throws Exception{
        validateAccess(201);
    }

    @Test
    public void should_return400_when_userHasApiAccess_ButSendsEmbargoedCOO() throws Exception{
        COO = "SD";
        ClientResponse response = validateAccess(400);
        String messageResponse = legalTagUtils.getResult(response, 400, String.class);
        assertEquals("{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"{\\\"errors\\\":[\\\"Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: [SD].\\\"]}\"}" , messageResponse);
    }

    @Test
    public void should_notAllowCreationOfLegalTag_when_countryOfOriginIsSetToARestrictedCountry() throws Exception {
        COO = "BV";
        ClientResponse response = validateAccess(400);
        String messageResponse = legalTagUtils.getResult(response, 400, String.class);
        assertEquals("{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"{\\\"errors\\\":[\\\"Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: [BV].\\\"]}\"}", messageResponse);
    }

    @Test
    public void should_return400_when_givenEmptyBody() throws Exception{
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), "{}", getQuery());
        assertEquals(400, response.getStatus());
    }

    @Test
    public void should_allowCreationOfLegalTag_when_3rdPArtyDataHasAValidExpirationDate() throws Exception {
        ClientResponse response = legalTagUtils.create("US", name, "", "Third Party Data");
        String messageResponse = legalTagUtils.getResult(response, 400, String.class);
        assertTrue(messageResponse.indexOf("You need to set an expiration date and contract ID") >= 0);
        response = legalTagUtils.create("US", name, "2099-12-25", "Third Party Data");
        legalTagUtils.getResult(response, 201, String.class);
    }

    @Test
    public void should_createLegalTag_with_descriptionNotSupplied() throws Exception{
        ClientResponse response = legalTagUtils.create("US", name, "2029-12-12", "Transferred Data", TestUtils.getMyDataPartition(), null);
        legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
    }

    @Test
    public void should_createLegalTag_when_expDateNotSupplied_and_dataTypeIsTranferred() throws Exception{
        ClientResponse response = legalTagUtils.create("US", name, null, "Transferred Data", TestUtils.getMyDataPartition(), "my desc");
        LegalTagUtils.ReadableLegalTag lt =legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, lt.name);
        assertTrue(lt.name.contains(TestUtils.getMyDataPartition()));
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToAnUnrestrictedCountry_andThen_notAllowCreationOfARecordWithTheSameKind() throws Exception {
        ClientResponse response = legalTagUtils.create("US", name);
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);

        response = legalTagUtils.create("US", name);
        String error = legalTagUtils.getResult(response, 409, String.class);
        assertTrue(error.contains("\"message\":\"A LegalTag already exists for the given name"));
    }

    @Test
    public void should_return400_CreationOfALegalTag_when_countryOfOriginIsSetToDefaultResidencyRiskCountry_andThen_None_ofThe_Datatype_is_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("MX", name);
        String messageResponse = legalTagUtils.getResult(response, 400, String.class);
        assertEquals("{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"{\\\"errors\\\":[\\\"Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: [MX].\\\"]}\"}", messageResponse);
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToDefaultResidencyRiskCountry_andThen_input_Datatype_is_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("GG", name,"Transferred Data");
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);
        assertEquals("Transferred Data", readableLegalTags.properties.dataType);
    }
    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNoRestrictionCountry_andThen_input_Datatype_is_not_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("TH", name, "First Party Data");
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);
        assertEquals("First Party Data", readableLegalTags.properties.dataType);
    }
    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNoRestrictionCountry_andThen_input_Datatype_is_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("JP", name, "Transferred Data");
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);
        assertEquals("Transferred Data", readableLegalTags.properties.dataType);
    }
    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNotAssignedCountry_andThen_input_Datatype_is_not_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("JP", name, "First Party Data");
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);
        assertEquals("First Party Data", readableLegalTags.properties.dataType);
    }
    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNotAssignedCountry_andThen_input__is_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("TH", name, "Transferred Data");
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);
        assertEquals("Transferred Data", readableLegalTags.properties.dataType);
    }

    // following two tests prove the older version config file in tenant would also work
    @Test
    public void should_allowCreationOfALegalTag_When_countryOfOriginIsSetToClientConsentRequiredInTenant_andDataTypeIsTransferredData() throws Exception{
        legalTagUtils.uploadTenantTestingConfigFile();
        ClientResponse response = legalTagUtils.create("MY", name, "Transferred Data");
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);
        assertEquals("Transferred Data", readableLegalTags.properties.dataType);
    }

    @Test
    public void should_allowCreationOfALegalTag_When_countryOfOriginIsSetToClientConsentRequiredInTenant_andDataTypeIsFirstPartyData() throws Exception{
        legalTagUtils.uploadTenantTestingConfigFile();
        ClientResponse response = legalTagUtils.create("US", name, "First Party Data");
        LegalTagUtils.ReadableLegalTag readableLegalTags = legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);
        assertEquals(name, readableLegalTags.name);
        assertEquals("First Party Data", readableLegalTags.properties.dataType);
    }

    @Test
    public void should_return400_CreationOfALegalTag_when_countryOfOriginIsSetToEmbargoCountry_andThen_input_Datatype_is_exluded_from_Dataresidency() throws Exception {
        ClientResponse response = legalTagUtils.create("IR", name, "Transferred Data");
        String messageResponse = legalTagUtils.getResult(response, 400, String.class);
        assertEquals("{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"{\\\"errors\\\":[\\\"Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: [IR].\\\"]}\"}", messageResponse);
    }
    @Test
    public void should_onlyLetAMaximumOf1LegaltagBeCreated_when_tryingToCreateMultipleVersionsOfTheSameContractAtTheSameTime() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<ClientResponse>> tasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Callable<ClientResponse> task = () -> {
                try {
                    return legalTagUtils.create(name);
                } catch (Exception ex) {
                    return null;
                }
            };
            tasks.add(task);
        }

        List<Future<ClientResponse>> responses = executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);

        int sucessResponseCount = 0;
        int non409ErrorResponseCount = 0;
        for (Future<ClientResponse> future : responses) {
            if (future.get().getStatus() == 201)
                sucessResponseCount++;
            else if (future.get().getStatus() != 409)
                non409ErrorResponseCount++;
        }

        assertTrue("Expected 1 successful response. Actual " + sucessResponseCount, sucessResponseCount <= 1);
        assertEquals(0, non409ErrorResponseCount);
    }

    @Override
    protected String getBody(){
        return LegalTagUtils.getBody(COO, name);
    }
    @Override
    protected String getApi() {
        return "legaltags";
    }
    @Override
    protected String getHttpMethod() {
        return "POST";
    }
}
