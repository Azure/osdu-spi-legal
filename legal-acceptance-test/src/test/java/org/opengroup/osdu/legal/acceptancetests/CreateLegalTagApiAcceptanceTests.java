package org.opengroup.osdu.legal.acceptancetests;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.legal.LegalTag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public final class CreateLegalTagApiAcceptanceTests extends LegalAcceptanceTests {

    private static final String INVALID_COUNTRY_OF_ORIGIN_MESSAGE =
        "Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: [%s].";

    @Test
    public void should_returnCreated_when_userHasApiAccess() {
        HttpResponse<LegalTag> response = legalTagClient.create(createLegalTag("US", name));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
    }

    @Test
    public void should_return400_when_userHasApiAccess_ButSendsEmbargoedCOO() {
        ClientException exception = expectClientError(
            () -> legalTagClient.create(createLegalTag("SD", name)));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
        assertEquals(validationError(String.format(INVALID_COUNTRY_OF_ORIGIN_MESSAGE, "SD")),
            exception.getError());
    }

    @Test
    public void should_notAllowCreationOfLegalTag_when_countryOfOriginIsSetToARestrictedCountry() {
        // BV has residency risk "Default" but is allowed for "Transferred Data" only (see DefaultCountryCode.json).
        ClientException exception = expectClientError(
            () -> legalTagClient.create(createLegalTag("BV", name, "First Party Data")));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
        assertEquals(validationError(String.format(INVALID_COUNTRY_OF_ORIGIN_MESSAGE, "BV")),
            exception.getError());
    }

    @Test
    public void should_return400_when_givenEmptyBody() {
        ClientException exception = expectClientError(() -> legalTagClient.create(emptyLegalTag()));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void should_allowCreationOfLegalTag_when_3rdPArtyDataHasAValidExpirationDate() {
        ClientException exception = expectClientError(
            () -> legalTagClient.create(createLegalTag("US", name, null, "Third Party Data",
                "Acceptance test legal tag")));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
        assertEquals(validationError("You need to set an expiration date and contract ID."),
            exception.getError());
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("US", name, "2099-12-25", "Third Party Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
    }

    @Test
    public void should_createLegalTag_with_descriptionNotSupplied() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("US", name, "2029-12-12", "Transferred Data", null));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
    }

    @Test
    public void should_createLegalTag_when_expDateNotSupplied_and_dataTypeIsTransferred() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("US", name, null, "Transferred Data", "my desc"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        LegalTag lt = response.body();
        assertEquals(name, lt.name());
        assertTrue(lt.name().contains(servicesConfig.getDataPartitionId()));
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToAnUnrestrictedCountry_andThen_notAllowCreationOfARecordWithTheSameKind() {
        HttpResponse<LegalTag> createResponse = legalTagClient.create(createLegalTag("US", name));
        assertJsonResponse(createResponse, HttpStatus.SC_CREATED);
        assertEquals(name, createResponse.body().name());

        ClientException exception = expectClientError(
            () -> legalTagClient.create(createLegalTag("US", name, "2099-12-25",
                "Transferred Data", "Acceptance test legal tag")));
        assertEquals(HttpStatus.SC_CONFLICT, exception.getStatusCode());
        assertTrue(exception.getError().getMessage()
            .contains("A LegalTag already exists for the given name"));
    }

    @Test
    public void should_return400_CreationOfALegalTag_when_countryOfOriginIsSetToDefaultResidencyRiskCountry_andThen_None_ofThe_Datatype_is_excluded_from_DataResidency() {
        ClientException exception = expectClientError(
            () -> legalTagClient.create(createLegalTag("MX", name, "2099-12-25",
                "Transferred Data", "Acceptance test legal tag")));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
        assertEquals(validationError(String.format(INVALID_COUNTRY_OF_ORIGIN_MESSAGE, "MX")),
            exception.getError());
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToDefaultResidencyRiskCountry_andThen_input_Datatype_is_excluded_from_DataResidency() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("GG", name, "Transferred Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        assertEquals(name, response.body().name());
        assertEquals("Transferred Data", response.body().properties().dataType());
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNoRestrictionCountry_andThen_input_Datatype_is_not_excluded_from_DataResidency() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("TH", name, "First Party Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        assertEquals(name, response.body().name());
        assertEquals("First Party Data", response.body().properties().dataType());
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNoRestrictionCountry_andThen_input_Datatype_is_excluded_from_DataResidency() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("JP", name, "Transferred Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        assertEquals(name, response.body().name());
        assertEquals("Transferred Data", response.body().properties().dataType());
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNotAssignedCountry_andThen_input_Datatype_is_not_excluded_from_DataResidency() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("JP", name, "First Party Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        assertEquals(name, response.body().name());
        assertEquals("First Party Data", response.body().properties().dataType());
    }

    @Test
    public void should_allowCreationOfALegalTag_when_countryOfOriginIsSetToNotAssignedCountry_andThen_input_is_excluded_from_DataResidency() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("TH", name, "Transferred Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        assertEquals(name, response.body().name());
        assertEquals("Transferred Data", response.body().properties().dataType());
    }

    @Test
    public void should_allowCreationOfALegalTag_When_countryOfOriginIsSetToClientConsentRequiredInTenant_andDataTypeIsTransferredData() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("MY", name, "Transferred Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);
        assertEquals(name, response.body().name());
        assertEquals("Transferred Data", response.body().properties().dataType());
    }

    @Test
    public void should_allowCreationOfALegalTag_When_countryOfOriginIsSetToClientConsentRequiredInTenant_andDataTypeIsFirstPartyData() {
        HttpResponse<LegalTag> response = legalTagClient.create(
            createLegalTag("US", name, "First Party Data"));
        assertJsonResponse(response, HttpStatus.SC_CREATED);

        log.info(">>> {}", response.body().name());
        log.info(">>> {}", response.body().properties().dataType());

        assertEquals(name, response.body().name());
        assertEquals("First Party Data", response.body().properties().dataType());
    }

    @Test
    public void should_return400_CreationOfALegalTag_when_countryOfOriginIsSetToEmbargoCountry_andThen_input_Datatype_is_excluded_from_DataResidency() {
        ClientException exception = expectClientError(
            () -> legalTagClient.create(createLegalTag("IR", name, "2099-12-25",
                "Transferred Data", "Acceptance test legal tag")));
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
        assertEquals(validationError(String.format(INVALID_COUNTRY_OF_ORIGIN_MESSAGE, "IR")),
            exception.getError());
    }

    @Test
    public void should_onlyLetAMaximumOf1LegalTagBeCreated_when_tryingToCreateMultipleVersionsOfTheSameContractAtTheSameTime() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> createStatusCode(name));
        }

        List<Future<Integer>> responses = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS));

        int successResponseCount = 0;
        int non409ErrorResponseCount = 0;
        for (Future<Integer> future : responses) {
            int statusCode = future.get();
            if (statusCode == HttpStatus.SC_CREATED) {
                successResponseCount++;
            } else if (statusCode != HttpStatus.SC_CONFLICT) {
                non409ErrorResponseCount++;
            }
        }

        assertTrue(successResponseCount <= 1, "Expected 1 successful response. Actual " + successResponseCount);
        assertEquals(0, non409ErrorResponseCount);
    }

    private int createStatusCode(String legalTagName) {
        try {
            return legalTagClient.create(createLegalTag(legalTagName)).statusCode();
        } catch (ClientException exception) {
            return exception.getStatusCode();
        }
    }
}
