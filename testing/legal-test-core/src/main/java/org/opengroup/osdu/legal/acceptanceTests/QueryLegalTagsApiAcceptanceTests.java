package org.opengroup.osdu.legal.acceptanceTests;

import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Arrays;

public abstract class QueryLegalTagsApiAcceptanceTests extends AcceptanceBaseTest {

    protected String name;
    protected static final List<Integer> OK_VALUES = Arrays.asList(200, 405);
    protected static final List<Integer> BAD_REQUEST_VALUES = Arrays.asList(400, 405);

    @Test
    public void should_return400Error_when_givingInvalidPayload1() throws Exception {
        ClientResponse response = send("{\"notValid\":\"payload\"}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(String.format("Expected a 400 or 405, but got %d", response.getStatus()),
            BAD_REQUEST_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200E_when_giving_empty_queryList() throws Exception {
        ClientResponse response = send("{\"queryList\":[]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return400Error_when_givingInvalidPayload_no_query_string() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(BAD_REQUEST_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_when_giving_notfound_queryList1() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name==notfound\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_when_giving_notfound_queryList2() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=notfound\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_queryList_countryOfOrigin() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"countryOfOrigin=US\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_queryList_dataType() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"dataType=public\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_queryList_personalData1() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"personalData=No\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_queryList_personalData2() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"personalData=No Personal\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_queryList_exportClassification() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"exportClassification=EAR99\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_queryList_expirationDate() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"expirationDate between (2023-01-01, 2099-12-31)\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return400Error_when_givingInvalidPayload_no_attribute() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"=test\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(BAD_REQUEST_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_union_strange() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=*?\"],\"operatorList\":[\"union\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_without_operator() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_union() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\"],\"operatorList\":[\"union\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_intersection() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\"],\"operatorList\":[\"intersection\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_add() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\"],\"operatorList\":[\"add\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_add_with_two() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\", \"description=test\"],\"operatorList\":[\"add\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_union_with_two1() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\", \"description=test\"],\"operatorList\":[\"union\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_intersection_with_two() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\", \"description=test\"],\"operatorList\":[\"intersection\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_intersection_with_three() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\", \"description=test\", \"exportClassification=EAR99\"],\"operatorList\":[\"intersection\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_union_with_three() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\", \"description=test\", \"exportClassification=EAR99\"],\"operatorList\":[\"union\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_free_text1() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"any=test\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_free_text2() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"test\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_with_match_name_operator_union_with_two2() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"name=test\", \"description=test\"],\"operatorList\":[\"union\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_attribute_does_not_exist() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"doesnotexist=test\"],\"operatorList\":[\"union\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
    }

    @Test
    public void should_return200_extension_properties() throws Exception {
        ClientResponse response = send("{\"queryList\":[\"AgreementPartyType=enabled\"],\"operatorList\":[\"union\"]}", "?valid=true", TestUtils.getMyDataPartition());
        assertTrue(OK_VALUES.contains(response.getStatus()));
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
        return "legaltags:query";
    }

    @Override
    protected String getHttpMethod() {
        return "POST";
    }
}