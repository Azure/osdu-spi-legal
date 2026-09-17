package org.opengroup.osdu.legal.tags;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.FeatureFlagController;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dto.*;
import org.opengroup.osdu.legal.tags.util.PersistenceExceptionToAppExceptionMapper;

import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagServiceTests {
    private PersistenceExceptionToAppExceptionMapper mapper = new PersistenceExceptionToAppExceptionMapper();
    @Mock
    private LegalTagConstraintValidator validator;
    @Mock
    private ILegalTagRepositoryFactory reposMock;
    @Mock
    private ILegalTagRepository legalTagRepositoryMock;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private ILegalTagPublisher messagePublisherMock;
    @Mock
    private JaxRsDpsLog log;
    @Mock
    private FeatureFlagController featureFlagControllerMock;

    @InjectMocks
    private LegalTagService sut;


    @BeforeEach
    public void setup() {
        sut.exceptionMapper = mapper;
        when(validator.getErrors(any())).thenReturn(null);
    }

    @Test
    public void should_notCreateLegalTag_when_givenNullContractArg() {
        sut = createSut();

        LegalTagDto result = sut.create(null, "tenant");

        assertEquals(null, result);
    }

    @Test
    public void should_createLegalTag_when_givenContractWithWriteAccess() {
        sut = createSut();

        LegalTagDto legalTag = LegalTestUtils.createValidLegalTagDto("test-1");

        LegalTagDto result = sut.create(legalTag, "test");

        assertEquals(legalTag.getName(), result.getName());
        verify(validator, times(2)).isValidThrows(any());
    }

    @Test
    public void should_logAuditLog_when_LegalTagIsCreated() {
        LegalTag output = new LegalTag();
        output.setName("kind2");
        LegalTagDto legalTagDto = LegalTestUtils.createValidLegalTagDto("account-1");
        LegalTagService testService = createSut(output);

        testService.create(legalTagDto, "tenant2");

        verify(auditLogger).createdLegalTagSuccess(any());
    }

    @Test
    public void should_assignVersionAndId_when_creatingLegalTag() {
        sut = createSut();
        LegalTagDto legalTagDto = LegalTestUtils.createValidLegalTagDto("1");

        legalTagDto = sut.create(legalTagDto, "tenant");
        LegalTag legalTag = LegalTagDto.convertFrom(legalTagDto);
        assertEquals(-1306694706L, (long) legalTag.getId());
    }

    @Test
    public void should_setTenantNamePrefix_when_creatingLegalTagAndItIsNotAlreadyPrefixed() {
        LegalTag legalTag = LegalTestUtils.createValidLegalTag("mylegaltag");
        sut = createSut(legalTag);

        LegalTagDto legalTagDto = LegalTagDto.convertTo(legalTag);
        legalTagDto = sut.create(legalTagDto, "tenant1");

        assertEquals("tenant1-mylegaltag", legalTagDto.getName());
        assertEquals((Long) LegalTag.getDefaultId("tenant1-mylegaltag"), LegalTagDto.convertFrom(legalTagDto).getId());
    }

    @Test
    public void should_useTenantNameAsPrefix_and_ignoreAccountId() {
        LegalTag legalTag = LegalTestUtils.createValidLegalTag("mylegaltag");
        sut = createSut(legalTag);

        LegalTagDto legalTagDto = LegalTagDto.convertTo(legalTag);
        legalTagDto = sut.create(legalTagDto, "tenant1");

        assertEquals("tenant1-mylegaltag", legalTagDto.getName());
    }

    @Test
    public void should_setTenantNamePrefix_when_creatingLegalTagAndItIsAlreadyPrefixedButInDifferentCase() {
        sut = createSut();
        LegalTagDto legalTagDto = LegalTestUtils.createValidLegalTagDto("mylegaltag");
        legalTagDto.setName("Tenant1-" + legalTagDto.getName());

        legalTagDto = sut.create(legalTagDto, "tenant1");

        assertEquals("tenant1-Tenant1-mylegaltag", legalTagDto.getName());
        //verify isvalidthrows was called
        verify(validator, times(2)).isValidThrows(any());
    }

    @Test
    public void should_notSetTenantNamePrefix_when_creatingLegalTagAndItIsAlreadyPrefixed() {
        LegalTag legalTag = LegalTestUtils.createValidLegalTag("mylegaltag");
        legalTag.setName("tenant2-" + legalTag.getName());
        sut = createSut(legalTag);

        LegalTagDto dto = LegalTagDto.convertTo(legalTag);
        dto = sut.create(dto, "tenant2");

        assertEquals("tenant2-mylegaltag", dto.getName());
        assertEquals((Long) LegalTag.getDefaultId("tenant2-mylegaltag"), LegalTagDto.convertFrom(dto).getId());
    }

    @Test
    public void should_ReturnNull_When_GivenNullName() {
        sut = createSut();

        LegalTagDto result = sut.get(null, "tenant2");

        assertEquals(null, result);
    }

    @Test
    public void should_returnLegalTag_when_givenExistingName() {
        sut = createSut();

        LegalTagDto result = sut.get("mykind", "tenant2");

        assertEquals("kind2", result.getName());
    }

    @Test
    public void should_ReturnNull_When_GivenNameThatDoesNotExist() {
        sut = createSut(null);

        LegalTagDto result = sut.get("mykind", "tenant1");

        assertEquals(null, result);
    }

    @Test
    public void should_ReturnNull_When_ErrorOccurredLegalTagsListIsNull() {
        sut = createSut(null);
        when(legalTagRepositoryMock.get(any())).thenReturn(null);
        LegalTagDto result = sut.get("mykind", "tenant1");

        assertEquals(null, result);
    }

    @Test
    public void should_returnFalse_When_GivenNullNameToDelete() {
        sut = createSut();

        Boolean result = sut.delete("project1", null, DpsHeaders.createFromMap(new HashMap<>()), "tenant1");

        assertFalse(result);
    }

    @Test
    public void should_returnFalse_When_GivenNullHeadersInDelete() {
        sut = createSut();

        Boolean result = sut.delete("project1", "name", null, "tenant");

        assertFalse(result);
    }

    @Test
    public void should_returnFalse_When_DeletingLegalTagErrorOccurred() {
        sut = createSut();
        when(legalTagRepositoryMock.delete(any())).thenReturn(false);
        Boolean result = sut.delete("project1", "name", DpsHeaders.createFromMap(new HashMap<>()), "tenant");

        assertFalse(result);
    }

    @Test
    public void should_returnTrue_When_DeletingWithWriteAccessAndNoMatchingRecords() {
        sut = createSut();

        Boolean result = sut.delete("project1", "name", DpsHeaders.createFromMap(new HashMap<>()), "tenant2");

        assertTrue(result);
    }

    @Test
    public void should_returnTrue_When_DeletingLegalTagWhichDoesNotExist() {
        sut = createSut(null);

        Boolean result = sut.delete("project1", "name", DpsHeaders.createFromMap(new HashMap<>()), "tenant2");

        assertTrue(result);
    }

    @Test
    public void should_returnNull_when_updateIsGivenNullLegalTagOrHeaders() {
        sut = createSut();

        LegalTagDto result = sut.update(null, "tenant1");
        assertNull(result);

        result = sut.update(new UpdateLegalTag(), null);
        assertNull(result);
    }

    @Test
    public void should_returnNull_when_updateStatusIsGivenNullLegalTagOrHeaders() {
        sut = createSut();

        LegalTagDto result = sut.updateStatus(null, true, "tenant1");
        assertNull(result);

        result = sut.updateStatus("hi", true, null);
        assertNull(result);
    }

    @Test
    public void should_throwAppException_whenUpdateStatus_IsGivenNonExistingLegalTag() {
        sut = createSut();
        when(legalTagRepositoryMock.get(any())).thenReturn(null);

        try {
            sut.updateStatus("legaltag2023", true, "tenant1");
            fail("Expected error");
        } catch (AppException ex) {
            assertEquals(404, ex.getError().getCode());
        }
    }

    @Test
    public void should_throwAppException400_when_legalTagDoesNotAlreadyExistOnUpdate() {
        sut = createSut(null);

        try {
            sut.update(LegalTestUtils.createUpdateLegalTag("name"), "tenant2");
            fail("Expected error");
        } catch (AppException ex) {
            assertEquals(404, ex.getError().getCode());
        }
    }

    @Test
    public void should_returnNullOnUpdate_when_repositoryReturnsNull() {
        UpdateLegalTag updateTag = LegalTestUtils.createUpdateLegalTag("name");
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        sut = createSut(input);
        when(legalTagRepositoryMock.update(input)).thenReturn(null);

        LegalTagDto result = sut.update(updateTag, "tenant2");
        assertNull(result);
    }

    @Test
    public void should_returnReadableLegalTag_when_updateSucceeds() {
        UpdateLegalTag updateTag = LegalTestUtils.createUpdateLegalTag("name");
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        sut = createSut(input);
        when(legalTagRepositoryMock.update(input)).thenReturn(input);

        LegalTagDto result = sut.update(updateTag, "tenant2");

        assertEquals(result.getName(), updateTag.getName());
        assertEquals(result.getProperties().getContractId(), updateTag.getContractId());
        assertEquals(result.getProperties().getExpirationDate(), updateTag.getExpirationDate());
        assertEquals(result.getProperties().getExtensionProperties(), updateTag.getExtensionProperties());
        assertEquals(result.getDescription(), updateTag.getDescription());
        verify(validator, times(1)).isValidThrows(any());
    }

    @Test
    public void should_returnReadableLegalTag_when_updateStatusSucceeds() {
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        sut = createSut(input);
        when(legalTagRepositoryMock.update(input)).thenReturn(input);

        assertFalse(input.getIsValid());

        LegalTagDto result = sut.updateStatus("name", true, "tenant1");

        assertEquals("name", result.getName());
        assertTrue(input.getIsValid());
    }

    @Test
    public void should_logAuditLog_when_updateStatusSucceeds() {
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        DpsHeaders headers = DpsHeaders.createFromMap(new HashMap<>());
        headers.put("Correlation-Id", "legal.123");
        sut = createSut(input);

        when(legalTagRepositoryMock.update(input)).thenReturn(input);

        sut.updateStatus("name", true, "tenant2");

        verify(auditLogger).updatedLegalTagSuccess(any());
    }

    @Test
    public void should_returnNullCollection_when_givenNonExistingNames() {
        sut = createSut();
        String[] input = new String[]{"abc", "123"};

        when(legalTagRepositoryMock.get(any())).thenReturn(new ArrayList<>());
        LegalTagDtos result = sut.getBatch(input, "tenant1");
        assertEquals(0, result.getLegalTags().size());
    }

    @Test
    public void should_returnNullCollection_when_givenNullNames() {
        sut = createSut();
        LegalTagDtos result = sut.getBatch(null, "tenant2");
        assertEquals(null, result);
    }

    @Test
    public void should_returnLegalTagCollection_when_givenExistingLegalTags() {
        sut = createSut();

        String[] input = new String[]{"abc", "123"};
        LegalTagDtos result = sut.getBatch(input, "tenant2");
        assertEquals(2, result.getLegalTags().size());
        verify(auditLogger).readLegalTagSuccess(any());
    }

    @Test
    public void should_returnLegalTags_when_givenLegalTagsIsCurrentlyInvalid() {
        sut = createSut();
        //when(validator.getErrors(any())).thenReturn("Not valid");

        String[] input = new String[]{"abc", "123"};
        LegalTagDtos result = sut.getBatch(input, "tenant1");

        assertEquals(2, result.getLegalTags().size());
        for (LegalTagDto lt : result.getLegalTags()) {
            assertEquals("kind2", lt.getName());
        }
        verify(auditLogger).readLegalTagSuccess(any());
    }

    @Test
    public void should_returnLegalTag_when_legalTagsIsNotCurrentlyValid() {
        sut = createSut();
        //when(validator.getErrors(any())).thenReturn("Not valid");

        LegalTagDto result = sut.get("abc", "tenant1");

        assertEquals("kind2", result.getName());
    }

    @Test
    public void should_returnNull_when_givenNullNames() {
        sut = createSut();
        InvalidTagsWithReason result = sut.validate(null, "tenant1");
        assertEquals(0, result.getInvalidLegalTags().size());
    }

    @Test
    public void should_returnNull_when_givenEmptyNames() {
        sut = createSut();
        InvalidTagsWithReason result = sut.validate(new String[]{}, "tenant1");
        assertEquals(0, result.getInvalidLegalTags().size());
    }

    @Test
    public void should_returnEmptyCollection_when_givenAllExistingAndValidNames() {
        sut = createSut();
        String[] input = new String[]{"kind2", "kind2"};
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(0, result.getInvalidLegalTags().size());
        verify(auditLogger).validateLegalTagSuccess();
    }

    @Test
    public void should_returnInvalidTagsWithNotFoundReason_when_givenNonexistingLegalTagNames() {
        sut = createSut();

        String[] input = new String[]{"kind2", "123"};
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(1, result.getInvalidLegalTags().size());
        for (InvalidTagWithReason invalidTagWithReason : result.getInvalidLegalTags()) {
            assertEquals("LegalTag not found", invalidTagWithReason.getReason());
        }
    }

    @Test
    public void should_returnAllNamesWithNotFound_when_givenAllNonexistingLegalTagNames() {
        sut = createSut();

        String[] input = new String[]{"abc", "123"};

        when(legalTagRepositoryMock.get(any())).thenReturn(new ArrayList<>());
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(2, result.getInvalidLegalTags().size());
        for (InvalidTagWithReason invalidTagWithReason : result.getInvalidLegalTags()) {
            assertEquals("LegalTag not found", invalidTagWithReason.getReason());
        }
    }

    @Test
    public void should_returnInvalidTagsWithNotValidReason_when_givenExistingLegalTagNames_which_areNotValid() {
        when(validator.getErrors(any())).thenReturn("Not valid");
        sut = createSut();

        String[] input = new String[]{"kind2", "kind2"};
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(2, result.getInvalidLegalTags().size());
        for (InvalidTagWithReason invalidTagWithReason : result.getInvalidLegalTags()) {
            assertEquals("Not valid", invalidTagWithReason.getReason());
        }
    }

    @Test
    public void should_returnValidTagsOnly_when_requestingOnlyValidTags() {
        sut = createSut();
        //when(validator.getErrors(any())).thenReturn("");
        LegalTagDtos result = sut.list(true, "tenant1");

        assertEquals(2, result.getLegalTags().size());
        verify(auditLogger).readLegalTagSuccess(any());
    }

    @Test
    public void should_sendDeletedLegalTagNameAndIncompliantStatusWithCorrelationIdToPubSub_when_legalTagIsDeleted() throws Exception {
        sut = createSut();

        Map<String, String> headers = new HashMap<>();
        headers.put(DpsHeaders.USER_EMAIL, "ash");
        headers.put(DpsHeaders.CORRELATION_ID, "123");

        DpsHeaders standardHeaders = DpsHeaders.createFromMap(headers);

        sut.delete("project1", "name", standardHeaders, "tenant1");
        verify(messagePublisherMock, times(1)).publish(any(), any(), any());
        verify(auditLogger).deletedLegalTagSuccess(any());
    }

    @Test
    public void should_notSendMessageToPubSub_when_deletedLegalTagDoesNotExist() throws Exception {
        sut = createSut(null);
        sut.delete("project1", "name", DpsHeaders.createFromMap(new HashMap<>()), "tenant1");

        verify(messagePublisherMock, never()).publish(any(), any(), any());
    }

    @Test
    public void should_notSendMessageToPubSub_when_GivenNullNameToDelete() throws Exception {
        sut = createSut();
        sut.delete("project1", null, DpsHeaders.createFromMap(new HashMap<>()), "tenant1");

        verify(messagePublisherMock, never()).publish(any(), any(), any());
    }

    @Test
    public void should_notCreateLegalTag_when_ExpirationDateIsInvalid() throws Exception {
        LegalTagDto legalTagDto = LegalTestUtils.createValidLegalTagDto("mylegaltag");
        long expirationDate = 5555555555555555L;
        legalTagDto.getProperties().setExpirationDate(new Date(expirationDate)) ;
        LegalTagService testService = createSut();

        AppException thrown = assertThrows(AppException.class, () -> {
            testService.create(legalTagDto, "tenant2");
        });

        // Assert: Check if the exception has the expected status code, error type, and message.
        assertEquals(400, thrown.getError().getCode(), "Expected 400 BadRequest error code");
        assertEquals("BadRequest", thrown.getError().getReason(), "Expected BadRequest error type");
    }

    @Test
    public void should_notUpdateLegalTag_when_ExpirationDateIsInvalid() throws Exception {
        UpdateLegalTag updateLegalTagDto = LegalTestUtils.createUpdateLegalTag("mylegaltag");
        long expirationDate = 5555555555555555L;
        updateLegalTagDto.setExpirationDate(new Date(expirationDate)) ;
        LegalTagService testService = createSut();

        AppException thrown = assertThrows(AppException.class, () -> {
            testService.update(updateLegalTagDto, "tenant2");
        });

        // Assert: Check if the exception has the expected status code, error type, and message.
        assertEquals(400, thrown.getError().getCode(), "Expected 400 BadRequest error code");
        assertEquals("BadRequest", thrown.getError().getReason(), "Expected BadRequest error type");
    }

    @Test
    public void should_returnLegalTag_when_givenExistingNames() {
        sut = createSut();

        LegalTagDto result = sut.get("mykind", "tenant1");

        assertEquals("kind2", result.getName());

        result = sut.get("tenant1-mykind", "tenant1");

        assertEquals("kind2", result.getName());
    }

    @Test
    public void should_returnMatchedLegalTags() {
        sut = createSutWithExtensionProperties_test1();

        Collection<LegalTag> legalTagDtos = sut.listLegalTag(true, "tenant1");

        assertFalse(legalTagDtos.isEmpty(), "collection size > 0");

    }

    @Test
    public void test_attribute_match() {
        // attributes expiration, countryOfOrigin, any
        LegalTag output = new LegalTag();
        output.setName("opendes-osdu-testing-1");
        output.setDescription("legal tag description test");
        Properties properties = new Properties();
        properties.setCountryOfOrigin(Collections.singletonList("US"));
        properties.setContractId("123450");
        properties.setContractId("123450");
        properties.setExpirationDate(Date.valueOf("2023-11-20"));
        output.setProperties(properties);
        output.setIsValid(true);
        sut = createSut(output);
        Boolean found = sut.checkAttributeForMatch("expirationDate", "2023-11-20", output);
        assert(found.equals(true));
        Boolean found2 = sut.checkAttributeForMatch("any", "test", output);
        assert(found2.equals(true));
        Boolean found3 = sut.checkAttributeForMatch("countryOfOrigin", "US", output);
        assert(found3.equals(true));
        Boolean found4 = sut.checkAttributeForMatch("dataType", "US", output);
        assert(found4.equals(false));

        HashMap<String, Object> extensionPropertiesMap = new HashMap<>();
        extensionPropertiesMap.put("TestBoolean", false);
        properties.setExtensionProperties(extensionPropertiesMap);
    }

    @Test
    public void test_attribute_match_returns_false_for_unknown_property() {
        // An attribute that is readable on neither bean but contains "any" reaches the
        // default branch of checkAttributeForMatch. It must report "no match" rather than
        // raise NotReadablePropertyException, which the caller would see as HTTP 500.
        LegalTag output = new LegalTag();
        output.setName("opendes-osdu-testing-1");
        Properties properties = new Properties();
        properties.setCountryOfOrigin(Collections.singletonList("US"));
        properties.setContractId("123450");
        properties.setExpirationDate(Date.valueOf("2023-11-20"));
        output.setProperties(properties);
        output.setIsValid(true);
        sut = createSut(output);

        assertFalse(sut.checkAttributeForMatch("anything", "US", output));
        assertFalse(sut.checkAttributeForMatch("company.any", "US", output));
    }

    public void test_properties(String testQuery, Boolean expectFound) {
        sut = createSutWithExtensionProperties_test1();

        QueryLegalTag searchLegalTag = new QueryLegalTag();
        searchLegalTag.setQueryList(Collections.singletonList(testQuery));

        LegalTagDtos resultDto = sut.queryLegalTag(searchLegalTag, true, "tenant1");
        if (expectFound) {
            assertFalse(resultDto.getLegalTags().isEmpty(), String.format("expected collection isNotEmpty, Query: %s", testQuery));
        } else {
            assertTrue(resultDto.getLegalTags().isEmpty(), String.format("expected collection isEmpty, Query: %s", testQuery));
        }

    }

    public void test_properties(String testQuery, Boolean expectFound, String operator) {
        sut = createSutWithExtensionProperties_test1();

        QueryLegalTag searchLegalTag = new QueryLegalTag();
        searchLegalTag.setQueryList(Collections.singletonList(testQuery));
        searchLegalTag.setOperatorList(Collections.singletonList(operator));

        LegalTagDtos resultDto = sut.queryLegalTag(searchLegalTag, true, "tenant1");
        if (expectFound) {
            assertFalse(resultDto.getLegalTags().isEmpty(), String.format("expected collection isNotEmpty, Query: %s", testQuery));
        } else {
            assertTrue(resultDto.getLegalTags().isEmpty(), String.format("expected collection isEmpty, Query: %s", testQuery));
        }

    }

    public void test_properties_exceptions(String testQuery, String message) {

        Exception e = assertThrows(AppException.class, ()->{
            test_properties(testQuery, true);
        });
        String m = e.getMessage();
        assertEquals(m, message);
    }

    public void test_properties_exceptions(String testQuery, String message, String operator) {

        Exception e = assertThrows(AppException.class, ()->{
            test_properties(testQuery, true, operator);
        });
        String m = e.getMessage();
        assertEquals(m, message);
    }

    @Test
    public void test_legaltag_exceptions_whenExceptionThrown_thenAssertionSucceeds() {
        // test exceptions
        test_properties_exceptions("[foo=]", "Error parsing attribute query, expected attribute=string");
        test_properties_exceptions("[any=]", "Error parsing attribute query, expected attribute=string");
        test_properties_exceptions("[]", "Error Null Search Query");
        test_properties_exceptions("", "Error Null Search Query");
        test_properties_exceptions("", "Error parsing operator list, expected intersection or union", "foo");
        test_properties_exceptions("[name=test]", "Error parsing operator list, expected intersection or union", "bar");
    }

    @Test
    public void test_individual_legaltag_properties()
    {
        // tag
        test_properties("[name=opendes-osdu-testing-1]", true);
        test_properties("[description=legal tag description test]", true);
        // properties
        test_properties("[countryOfOrigin=US]", true);
        test_properties("[contractId=123450]", true);
        test_properties("[expirationDate=2023-11]", true);
        test_properties("[expirationDate between (2023-03-04,2023-11-22)]", true);
        test_properties("[originator=Schul]", true);
        test_properties("[originator=schul]", true);
        test_properties("[dataType=third party]", true);
        test_properties("[securityClassification=private]", true);
        test_properties("[personalData=no personal]", true);
        test_properties("[exportClassification=ear99]", true);
        // extension properties
        test_properties("[AgreementIdentifier=test]", true);
        test_properties("[EffectiveDate=2023-01]", true);
        test_properties("[TerminationDate=2099-12]", true);
        test_properties("[CountryCode=GBL]", true);
        test_properties("[TestingMap=ABC]", true);
        test_properties("[AffiliateEnablementIndicator=true]", true);
        test_properties("[expirationDate between (2023-03-04,2023-11-22)]", true);
        test_properties("[TestBoolean1=false]", true);
        test_properties("[TestBoolean2=true]", true);
        test_properties("[TestBoolean5=true]", true);
        test_properties("[TestBoolean4=true]", true);
        test_properties("[TestBoolean3=false]", true);
        test_properties("[TestBoolean9=false]", false);
        // any
        when(featureFlagControllerMock.isLegalTagQueryApiFreeTextAllFieldsFlagEnabled()).thenReturn(true);
        test_properties("[test]", true);
        test_properties("[any=test]", true);
        test_properties("[notfoundhere]", false);
        // test nested extended properties
        test_properties("[AgreementParty=some test data]", true);
    }

    @Test
    public void test_operators()
    {
        test_properties("[name=opendes-osdu-testing-1]", true, "union");
        test_properties("[name=opendes-osdu-testing-1]", true, "intersection");
        test_properties("[name=opendes-osdu-testing-1]", true, "add");
    }

    @Test
    public void test_operators_empty()
    {
        sut = createSutWithExtensionProperties_test1();
        QueryLegalTag searchLegalTag = new QueryLegalTag();
        searchLegalTag.setQueryList(Collections.singletonList("[name=opendes-osdu-testing-1]"));
        searchLegalTag.setOperatorList(Collections.emptyList());

        LegalTagDtos resultDto = sut.queryLegalTag(searchLegalTag, true, "tenant1");
        assertFalse(resultDto.getLegalTags().isEmpty(), "collection size > 0");
    }

    private LegalTagService createSutWithExtensionProperties_test1() {
        LegalTag output = new LegalTag();
        output.setName("opendes-osdu-testing-1");
        output.setDescription("legal tag description test");
        output.setIsValid(true);
        Properties properties = new Properties();
        properties.setCountryOfOrigin(Collections.singletonList("US"));
        properties.setContractId("123450");
        properties.setExpirationDate(Date.valueOf("2023-11-20"));
        properties.setOriginator("Schulmberger");
        properties.setDataType("Third Party Data");
        properties.setSecurityClassification("Private");
        properties.setPersonalData("No Personal Data");
        properties.setExportClassification("EAR99");
        HashMap<String, Object> extensionPropertiesMap = new HashMap<>();
        extensionPropertiesMap.put("AgreementIdentifier", "dz-test-0");
        extensionPropertiesMap.put("EffectiveDate", "2023-01-00T00:00:00");
        extensionPropertiesMap.put("TerminationDate", "2099-12-31T00:00:00");
        extensionPropertiesMap.put("AffiliateEnablementIndicator", "true");
        extensionPropertiesMap.put("TestBoolean1", "this is false");
        extensionPropertiesMap.put("TestBoolean2", "this is true");
        extensionPropertiesMap.put("TestBoolean3", false);
        extensionPropertiesMap.put("TestBoolean4", true);
        extensionPropertiesMap.put("TestBoolean5", true);
        extensionPropertiesMap.put("TestBooleanString1", "true");
        extensionPropertiesMap.put("TestBooleanString2", "false");
        extensionPropertiesMap.put("TestBooleanArray1", "[true]");
        extensionPropertiesMap.put("TestBooleanArray2", "[false]");
        HashMap<String, Object> agreementPartyMap = new HashMap<String, Object>();
        agreementPartyMap.put("AgreementPartyType", "omnibus");
        agreementPartyMap.put("AgreementParty", "random things");
        HashMap<String, Object> dataCoverageMap = new HashMap<String, Object>();
        dataCoverageMap.put("Country", "osdu:master-data--GeopoliticalEntity:Global:");
        dataCoverageMap.put("CountryCode", "GBL");
        extensionPropertiesMap.put("DataCoverage", dataCoverageMap);

        ArrayList<HashMap> agreementList = new ArrayList<HashMap>();
        agreementList.add(agreementPartyMap);
        agreementPartyMap.put("AgreementPartyType", "test AgreementPartyType");
        agreementPartyMap.put("AgreementParty", "some test data");
        agreementList.add(agreementPartyMap);

        ArrayList<String> someArbitList = new ArrayList<String>();
        someArbitList.add("ABC");
        someArbitList.add("123");

        extensionPropertiesMap.put("TestingMap", someArbitList);

        extensionPropertiesMap.put("AgreementParties", agreementList);


        properties.setExtensionProperties(extensionPropertiesMap);
        output.setProperties(properties);
        return createSut(output);
    }

    private LegalTagService createSut() {
        LegalTag output = new LegalTag();
        output.setName("kind2");
        return createSut(output);
    }

    private LegalTagService createSut(LegalTag getOutput) {
        Answer<Long> answer = invocation -> getOutput.getId();
        when(legalTagRepositoryMock.create(any())).thenAnswer(answer);
        when(legalTagRepositoryMock.get(any())).thenReturn(Arrays.asList(getOutput, getOutput));
        when(legalTagRepositoryMock.list(any())).thenReturn(Arrays.asList(getOutput, getOutput));
        when(legalTagRepositoryMock.delete(any())).thenReturn(true);
        when(reposMock.get(any())).thenReturn(legalTagRepositoryMock);

        return sut;
    }
}
