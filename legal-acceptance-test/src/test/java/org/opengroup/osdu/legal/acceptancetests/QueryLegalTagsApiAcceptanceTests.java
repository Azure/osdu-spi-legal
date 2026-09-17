package org.opengroup.osdu.legal.acceptancetests;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.model.legal.MalformedQueryRequest;
import org.opengroup.osdu.core.test.client.model.legal.QueryLegalTag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class QueryLegalTagsApiAcceptanceTests extends LegalAcceptanceTests {

    private static final List<Integer> BAD_REQUEST_VALUES = Arrays.asList(
        HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_METHOD_NOT_ALLOWED);

    @Test
    public void should_return400Error_when_givingInvalidPayload() {
        expectQueryError(() -> legalTagClient.query(new MalformedQueryRequest("payload"), true),
            BAD_REQUEST_VALUES);
    }

    @Test
    public void should_return200E_when_giving_empty_queryList() {
        assertQuerySuccess(new QueryLegalTag(Collections.emptyList(), null, null, null, null));
    }

    @Test
    public void should_return400Error_when_givingInvalidPayload_no_query_string() {
        expectQueryError(() -> legalTagClient.query(query("name="), true), BAD_REQUEST_VALUES);
    }

    @Test
    public void should_return200_when_giving_notfound_queryList1() {
        assertQuerySuccess(query("name==notfound"));
    }

    @Test
    public void should_return200_when_giving_notfound_queryList2() {
        assertQuerySuccess(query("name=notfound"));
    }

    @Test
    public void should_return200_queryList_countryOfOrigin() {
        assertQuerySuccess(query("countryOfOrigin=US"));
    }

    @Test
    public void should_return200_queryList_dataType() {
        assertQuerySuccess(query("dataType=public"));
    }

    @Test
    public void should_return200_queryList_personalData1() {
        assertQuerySuccess(query("personalData=No"));
    }

    @Test
    public void should_return200_queryList_personalData2() {
        assertQuerySuccess(query("personalData=No Personal"));
    }

    @Test
    public void should_return200_queryList_exportClassification() {
        assertQuerySuccess(query("exportClassification=EAR99"));
    }

    @Test
    public void should_return200_queryList_expirationDate() {
        assertQuerySuccess(query("expirationDate between (2023-01-01, 2099-12-31)"));
    }

    @Test
    public void should_return400Error_when_givingInvalidPayload_no_attribute() {
        expectQueryError(() -> legalTagClient.query(query("=test"), true), BAD_REQUEST_VALUES);
    }

    @Test
    public void should_return200_with_match_name_operator_union_strange() {
        assertQuerySuccess(query(List.of("name=*?"), List.of("union")));
    }

    @Test
    public void should_return200_with_match_name_without_operator() {
        assertQuerySuccess(query("name=test"));
    }

    @Test
    public void should_return200_when_giving_empty_operatorList() {
        assertQuerySuccess(new QueryLegalTag(List.of("name=test"), Collections.emptyList(), null, null, null));
    }

    @Test
    public void should_return200_with_match_name_operator_union() {
        assertQuerySuccess(query(List.of("name=test"), List.of("union")));
    }

    @Test
    public void should_return200_with_match_name_operator_intersection() {
        assertQuerySuccess(query(List.of("name=test"), List.of("intersection")));
    }

    @Test
    public void should_return200_with_match_name_operator_add() {
        assertQuerySuccess(query(List.of("name=test"), List.of("add")));
    }

    @Test
    public void should_return200_with_match_name_operator_add_with_two() {
        assertQuerySuccess(query(List.of("name=test", "description=test"), List.of("add")));
    }

    @Test
    public void should_return200_with_match_name_operator_union_with_two1() {
        assertQuerySuccess(query(List.of("name=test", "description=test"), List.of("union")));
    }

    @Test
    public void should_return200_with_match_name_operator_intersection_with_two() {
        assertQuerySuccess(query(List.of("name=test", "description=test"), List.of("intersection")));
    }

    @Test
    public void should_return200_with_match_name_operator_intersection_with_three() {
        assertQuerySuccess(query(
            List.of("name=test", "description=test", "exportClassification=EAR99"),
            List.of("intersection")));
    }

    @Test
    public void should_return200_with_match_name_operator_union_with_three() {
        assertQuerySuccess(query(
            List.of("name=test", "description=test", "exportClassification=EAR99"),
            List.of("union")));
    }

    @Test
    public void should_return200_free_text1() {
        assertQuerySuccess(query("any=test"));
    }

    @Test
    public void should_return200_free_text2() {
        assertQuerySuccess(query("test"));
    }

    @Test
    public void should_return200_with_match_name_operator_union_with_two2() {
        assertQuerySuccess(query(List.of("name=test", "description=test"), List.of("union")));
    }

    @Test
    public void should_return200_attribute_does_not_exist() {
        assertQuerySuccess(query(List.of("doesnotexist=test"), List.of("union")));
    }

    @Test
    public void should_return200_extension_properties() {
        assertQuerySuccess(query(List.of("AgreementPartyType=enabled"), List.of("union")));
    }

    private void assertQuerySuccess(QueryLegalTag query) {
        try {
            assertJsonResponse(legalTagClient.query(query, true), HttpStatus.SC_OK);
        } catch (ClientException exception) {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, exception.getStatusCode());
        }
    }

    private void expectQueryError(Executable queryAction, List<Integer> expectedStatuses) {
        ClientException exception = assertThrows(ClientException.class, queryAction);
        assertTrue(expectedStatuses.contains(exception.getStatusCode()),
            () -> String.format("Expected one of %s but got %d", expectedStatuses, exception.getStatusCode()));
    }

    private static QueryLegalTag query(String... queryList) {
        return new QueryLegalTag(List.of(queryList), null, null, null, null);
    }

    private static QueryLegalTag query(List<String> queryList, List<String> operatorList) {
        return new QueryLegalTag(queryList, operatorList, null, null, null);
    }
}
