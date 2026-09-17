package org.opengroup.osdu.legal.acceptanceTests;

import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class ValidateLegalTagsApiAcceptanceTests extends AcceptanceBaseTest {

    protected String name;

    @Test
    public void should_return400Error_when_givingInvalidName() throws Exception {
        name = "invalid*name";
        validateAccess(400);
    }

    @Test
    public void should_return200_withNoInvalidTagsReturned_when_userHasOnlyReadDataAccess() throws Exception {
        name = LegalTagUtils.createRandomNameTenant();
        ClientResponse response = legalTagUtils.create("US", name);
        legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);

        response = send(LegalTagUtils.createRetrieveBatchBody(name), 200);

        LegalTagUtils.InvalidTagsWithReason invalidTagsWithReason = legalTagUtils.getResult(response, 200, LegalTagUtils.InvalidTagsWithReason.class);
        assertEquals(0, invalidTagsWithReason.invalidLegalTags.length);
        legalTagUtils.delete(name);
    }

    @Test
    public void should_return200_withNotFoundLegalTagNamesAndReason_when_givenNonexistingLegalTagNames() throws Exception {
        name = LegalTagUtils.getMyDataPartition() + "-" + "iDoNotExist";

        ClientResponse response = send(LegalTagUtils.createRetrieveBatchBody(name), 200);

        LegalTagUtils.InvalidTagsWithReason invalidTagsWithReason = legalTagUtils.getResult(response, 200, LegalTagUtils.InvalidTagsWithReason.class);
        assertEquals(1, invalidTagsWithReason.invalidLegalTags.length);

        LegalTagUtils.InvalidTagWithReason invalidTagWithReason = invalidTagsWithReason.invalidLegalTags[0];
        assertEquals(name, invalidTagWithReason.name);
        assertEquals("LegalTag not found", invalidTagWithReason.reason);
    }

    @Test
    public void should_return200_withLegalTagNamesAndInvalidExpirationDateReason_when_GivenExistingInvalidLegalTagNames() throws Exception {
        // Manually created in all env cause compliance api is not allowed invalid legaltag creation.
        name = "dps-integration-test-1566474656479";

        // Use the following commented code to recreate the legaltag in case the previous invalid legaltag got deleted.
        // You will need to manually change the expiration date to make the legaltag invalid in the datastore.
//        ClientResponse response = LegalTagUtils.create("US", name);
//        legalTagUtils.getResult(response, 201, LegalTagUtils.ReadableLegalTag.class);

        ClientResponse response = send(LegalTagUtils.createRetrieveBatchBody(legalTagUtils.getMyDataPartition() + "-" + name), 200);

        LegalTagUtils.InvalidTagsWithReason invalidTagsWithReason = legalTagUtils.getResult(response, 200, LegalTagUtils.InvalidTagsWithReason.class);
        assertEquals(1, invalidTagsWithReason.invalidLegalTags.length);

        LegalTagUtils.InvalidTagWithReason invalidTagWithReason = invalidTagsWithReason.invalidLegalTags[0];
        assertEquals(legalTagUtils.getMyDataPartition() + "-" + name, invalidTagWithReason.name);
        assertTrue(invalidTagWithReason.reason.startsWith("Expiration date must be a value in the future."));
    }

    @Test
    @Override
    public void should_return401_when_makingHttpRequestWithoutToken()throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        super.should_return401_when_makingHttpRequestWithoutToken();
    }

    @Override
    protected String getBody() {
        return LegalTagUtils.createRetrieveBatchBody(name);
    }

    @Override
    protected String getApi() {
        return "legaltags:validate";
    }

    @Override
    protected String getHttpMethod() {
        return "POST";
    }
}
