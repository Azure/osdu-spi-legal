package org.opengroup.osdu.legal.controller;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.FeatureFlagController;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.legal.tags.LegalTestUtils;
import org.opengroup.osdu.legal.tags.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ValidationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagControllerTests {

    @Mock
    private RequestInfo requestInfo;

    @Mock
    private LegalTagService legalTagService;

    @Mock
    private LegalTagCountriesService legalTagCountriesService;

    @Mock
    private AllowedLegaltagPropertyValues allowedLegalTagValues;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private TenantInfo tenantInfo;

    @Mock
    private DpsHeaders dpsHeaders;

    @Mock
    private FeatureFlagController featureFlagControllerMock;

    @InjectMocks
    private LegalTagController sut;

    @BeforeEach
    public void setup() {
        when(this.requestInfo.getHeaders()).thenReturn(dpsHeaders);
        when(this.requestInfo.getTenantInfo()).thenReturn(tenantInfo);
        when(this.tenantInfo.getName()).thenReturn("tenantName");
        when(this.tenantInfo.getProjectId()).thenReturn("projectId");

    }

    @Test
    public void should_return201AndNewLegalTag_when_givenValidLegalTag() {
        LegalTagDto legalTag = LegalTestUtils.createValidLegalTagDto("w");

        LegalTagDto newContracts = new LegalTagDto();
        newContracts.setName("kind1");
        when(legalTagService.create(any(), any())).thenReturn(newContracts);
        ResponseEntity<LegalTagDto> result = sut.createLegalTag(legalTag);

        assertEquals(201, result.getStatusCode().value());
        newContracts = (LegalTagDto) result.getBody();

        assertEquals("kind1", newContracts.getName());
    }

    @Test
    public void should_return200AndLegalTag_when_givenValidName() {
        LegalTagDto contract = LegalTestUtils.createValidLegalTagDto("k");

        when(legalTagService.get(any(), any())).thenReturn(contract);

        ResponseEntity<LegalTagDto> result = sut.getLegalTag("k");

        assertEquals(200, result.getStatusCode().value());
        LegalTagDto output = (LegalTagDto) result.getBody();

        assertEquals("k", output.getName());
    }

    @Test
    public void should_return404_when_givenNameThatDoesNotExist() {
        when(legalTagService.get(any(), any())).thenReturn(null);
        ResponseEntity<LegalTagDto> result = sut.getLegalTag("k");

        assertEquals(404, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    public void should_return500_when_deletingReturnsFalse() {
        ResponseEntity<HttpStatus> result = sut.deleteLegalTag("k");

        assertEquals(500, result.getStatusCode().value());
    }

    @Test
    public void should_return204_when_deletingReturnsTrue() {
        when(legalTagService.delete(any(), eq("k"), any(), any())).thenReturn(true);
        ResponseEntity<HttpStatus> result = sut.deleteLegalTag("k");

        assertEquals(204, result.getStatusCode().value());
    }

    @Test
    public void should_return200AndLegalTags_when_givenValidNames() {
        LegalTagDto tag = LegalTestUtils.createValidLegalTagDto("k");

        List<LegalTagDto> tags = Arrays.asList(tag, tag);
        LegalTagDtos output = new LegalTagDtos();
        output.setLegalTags(tags);
        when(legalTagService.getBatch(any(), any())).thenReturn(output);
        RequestLegalTags input = new RequestLegalTags();
        input.setNames(Arrays.asList("k", "k"));

        ResponseEntity<LegalTagDtos> result = sut.getLegalTags(input);

        assertEquals(200, result.getStatusCode().value());
        LegalTagDtos entity = (LegalTagDtos) result.getBody();

        assertEquals(2, entity.getLegalTags().size());
        assertEquals("k", Iterables.get(entity.getLegalTags(), 0).getName());
    }

    @Test
    public void should_return404_when_weHaventReturnedALegalTagForEachRequestedName() {
        LegalTagDto tag = LegalTestUtils.createValidLegalTagDto("k");

        List<LegalTagDto> tags = Arrays.asList(tag);
        LegalTagDtos output = new LegalTagDtos();
        output.setLegalTags(tags);
        when(legalTagService.getBatch(any(), any())).thenReturn(output);
        RequestLegalTags input = new RequestLegalTags();
        input.setNames(Arrays.asList("k", "l"));

        ResponseEntity<LegalTagDtos> result = sut.getLegalTags(input);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void should_return404_when_weReturnNullFromServiceRequest() {
        when(legalTagService.getBatch(any(), any())).thenReturn(null);
        RequestLegalTags input = new RequestLegalTags();
        input.setNames(Arrays.asList("k", "l"));

        ResponseEntity<LegalTagDtos> result = sut.getLegalTags(input);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void should_return200AndInvalidTagsWithReason_when_givenValidNames() {
        InvalidTagsWithReason invliadTagsWithReason = new InvalidTagsWithReason();

        when(legalTagService.validate(any(), any())).thenReturn(invliadTagsWithReason);
        RequestLegalTags input = new RequestLegalTags();
        input.setNames(Arrays.asList("k", "k"));

        ResponseEntity<InvalidTagsWithReason> result = sut.validateLegalTags(input);

        assertEquals(200, result.getStatusCode().value());
        InvalidTagsWithReason output = (InvalidTagsWithReason) result.getBody();

        assertTrue(output.getInvalidLegalTags().isEmpty());
    }

    @Test
    public void should_return200_when_givenEmptyNames() {
        RequestLegalTags input = new RequestLegalTags();
        input.setNames(Arrays.asList("", ""));

        ResponseEntity<InvalidTagsWithReason> result = sut.validateLegalTags(input);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void should_return200AndNewLegalTag_when_givenValidUpdateLegalTag() {
        UpdateLegalTag newTag = LegalTestUtils.createUpdateLegalTag("ash1");
        LegalTagDto output = LegalTestUtils.createValidLegalTagDto("ash1");

        when(legalTagService.update(any(), any())).thenReturn(output);
        ResponseEntity<LegalTagDto> result = sut.updateLegalTag(newTag);

        assertEquals(200, result.getStatusCode().value());
        LegalTagDto resultEntity = (LegalTagDto) result.getBody();
        assertEquals("ash1", resultEntity.getName());
    }

    @Test
    public void should_return200AndLegalTags_when_listingLegalTags() {
        LegalTagDto tag = LegalTestUtils.createValidLegalTagDto("k");

        List<LegalTagDto> tags = Arrays.asList(tag, tag);
        LegalTagDtos output = new LegalTagDtos();
        output.setLegalTags(tags);
        when(legalTagService.list(anyBoolean(), any())).thenReturn(output);

        ResponseEntity<LegalTagDtos> result = sut.listLegalTags(true);

        assertEquals(200, result.getStatusCode().value());
        LegalTagDtos entity = (LegalTagDtos) result.getBody();

        assertEquals(2, entity.getLegalTags().size());
        assertEquals("k", Iterables.get(entity.getLegalTags(), 0).getName());
    }

    @Test
    public void should_ThrowValidationException_when_listingLegalTags_IfTenantInfoIsNull() {
        when(requestInfo.getTenantInfo()).thenReturn(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            sut.listLegalTags(true);
        });

        assertEquals("No tenant supplied", exception.getMessage());
    }

    @Test
    public void should_return200AndProperties_when_requestingGetAllProperties() {
        Set<String> dataTypes = new HashSet<String>() {{
            add("datatype");
        }};
        when(allowedLegalTagValues.getDataTypes()).thenReturn(dataTypes);
        Set<String> eccns = new HashSet<String>() {{
            add("eccns");
        }};
        when(allowedLegalTagValues.getEccns()).thenReturn(eccns);
        Set<String> personal = new HashSet<String>() {{
            add("personal");
        }};
        when(allowedLegalTagValues.getPersonalDataType()).thenReturn(personal);
        Set<String> security = new HashSet<String>() {{
            add("security");
        }};
        when(allowedLegalTagValues.getSecurityClassifications()).thenReturn(security);
        Map<String, String> coo = new HashMap<String, String>() {{
            put("CH", "china");
        }};
        when(legalTagCountriesService.getValidCOOs()).thenReturn(coo);
        Map<String, String> ordc = new HashMap<String, String>() {{
            put("US", "usa");
        }};
        when(legalTagCountriesService.getValidORDCs()).thenReturn(ordc);

        ResponseEntity<ReadablePropertyValues> result = sut.getLegalTagProperties();

        assertEquals(200, result.getStatusCode().value());
        ReadablePropertyValues entity = (ReadablePropertyValues) result.getBody();

        assertEquals(entity.getCountriesOfOrigin(), coo);
        assertEquals(entity.getOtherRelevantDataCountries(), ordc);
        assertEquals(entity.getExportClassificationControlNumbers(), eccns);
        assertEquals(entity.getPersonalDataTypes(), personal);
        assertEquals(entity.getSecurityClassifications(), security);
        assertEquals(entity.getDataTypes(), dataTypes);
    }

    @Test
    public void shouldCreateAuditLogs_when_getLegalTagProperties() {
        sut.getLegalTagProperties();

        verify(auditLogger).readLegalPropertiesSuccess(any());
    }

    @Test
    public void should_return200AndLegalTags_when_matchFound() {
        LegalTagDto tag = LegalTestUtils.createValidLegalTagDto("k");

        List<LegalTagDto> tags = Arrays.asList(tag, tag, tag);
        LegalTagDtos output = new LegalTagDtos();
        output.setLegalTags(tags);
        
        // isLegalQueryApi Feature Flag Enabled?
        when(featureFlagControllerMock.isLegalTagQueryApiFlagEnabled()).thenReturn(true);
       

        when(legalTagService.queryLegalTag(any(), anyBoolean(), any())).thenReturn(output);

        QueryLegalTag searchQuery = new QueryLegalTag();

        searchQuery.setQueryList(Collections.singletonList("[AgreementPartyType= PurchaseOrganisation]"));

        ResponseEntity<LegalTagDtos> result = sut.queryLegalTag(searchQuery, true);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        LegalTagDtos entity = (LegalTagDtos) result.getBody();

        assertEquals(3, entity.getLegalTags().size(), 0);
    }

    @Test
    public void should_return405_legal_query_api_disabled() {
        // isLegalQueryApi Feature Flag Enabled?
        when(featureFlagControllerMock.isLegalTagQueryApiFlagEnabled()).thenReturn(false);

        QueryLegalTag searchQuery = new QueryLegalTag();
        searchQuery.setQueryList(Collections.singletonList("[AgreementPartyType= PurchaseOrganisation]"));

        ResponseEntity<LegalTagDtos> result = sut.queryLegalTag(searchQuery, true);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, result.getStatusCode());
    }

}