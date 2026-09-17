package org.opengroup.osdu.legal.acceptanceTests;

import com.google.common.base.Strings;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public abstract class ListLegalTagsApiAcceptanceTests extends AcceptanceBaseTest {

    @Test
    public void should_return200_and_allValidLegalTags_when_sendingValidTrueParameter_And_notSendingValidParameter()throws Exception{
        ClientResponse response = send("", 200, "?valid=true", TestUtils.getMyDataPartition());
        LegalTagUtils.ReadableLegalTags result = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTags.class);
        System.out.println("number of lts:" + result.legalTags.length ) ;
        assertTrue(result.legalTags.length > 0);
        assertFalse(Strings.isNullOrEmpty(result.legalTags[0].name));
        assertFalse(Strings.isNullOrEmpty(result.legalTags[0].properties.countryOfOrigin[0]));

        ClientResponse response2 = send("", 200, "");
        LegalTagUtils.ReadableLegalTags result2 = legalTagUtils.getResult(response2, 200, LegalTagUtils.ReadableLegalTags.class);
        for(LegalTagUtils.ReadableLegalTag tag : result.legalTags){
            assertTrue(Arrays.stream(result2.legalTags).anyMatch(s -> tag.name.equals(s.name)));
        }
    }

    @Test
    public void should_returnDifferentResults_when_sendingValidParameterTrueOrFalse()throws Exception{
        ClientResponse response = send("", 200, "?valid=true");
        LegalTagUtils.ReadableLegalTags result = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTags.class);

        ClientResponse response2 = send("", 200, "?valid=false");
        LegalTagUtils.ReadableLegalTags result2 = legalTagUtils.getResult(response2, 200, LegalTagUtils.ReadableLegalTags.class);
        
        // Ensure no overlap between valid=true and valid=false results
        for(LegalTagUtils.ReadableLegalTag tag : result.legalTags){
            assertFalse(Arrays.stream(result2.legalTags).anyMatch(s -> tag.name.equals(s.name)));
        }
    }

    @Override
    protected String getBody(){
        return "";
    }

    @Override
    protected String getApi() {
        return "legaltags";
    }

    @Override
    protected String getHttpMethod() {
        return "GET";
    }
}
