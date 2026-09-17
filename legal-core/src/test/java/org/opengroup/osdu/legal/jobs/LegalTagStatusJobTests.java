package org.opengroup.osdu.legal.jobs;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.opengroup.osdu.legal.tags.LegalTagConstraintValidator;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;
import org.opengroup.osdu.legal.FeatureFlagController;
import org.opengroup.osdu.legal.config.LegalTagConfig;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Calendar;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagStatusJobTests {
    @Mock
    private LegalTagConstraintValidator validator;
    @Mock
    private LegalTagService legalTagServiceMock;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private ILegalTagPublisher messagePublisherMock;
    @Mock
    private IAboutToExpireLegalTagPublisher aboutToExpireLegalTagPublisherMock;
    @Mock
    private JaxRsDpsLog log;
    @Mock
    private FeatureFlagController featureFlagControllerMock;
    @Mock
    private Clock clock;
    @Mock
    private LegalTagConfig configMock;

    @InjectMocks
    LegalTagStatusJob sut;

    private Clock fixedClock;

    private DpsHeaders headers = new DpsHeaders();

    private final static LocalDate LOCAL_DATE = LocalDate.of(2022, 01, 01);

    @BeforeEach
    public void setup() {
        when(validator.getErrors(any())).thenReturn(null);
        headers.put(DpsHeaders.DATA_PARTITION_ID, "SIS-INTERNAL-HQ");
        headers.put(DpsHeaders.CORRELATION_ID, "12345-12345");
        headers.put(DpsHeaders.USER_EMAIL, "nonexistent@nonexisent.domain");
        // aboutToExpireFeatureFlag
        when(featureFlagControllerMock.isAboutToExpireFeatureFlagEnabled()).thenReturn(true);
        when(configMock.getExpirationAlerts()).thenReturn("1m,2w,1d");
        // mock LocalDate.now()
        fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @Test
    public void should_returnNoResults_when_legalTagsComplianceStatusHasNotChanged() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        sut = createSut(validLegalTags);
        //when(validator.getErrors(any())).thenReturn("");

        LegalTagJobResult result = sut.run("project1", headers, "tenant");
        assertEquals(0, result.statusChangedTags.getStatusChangedTags().size());
    }

    @Test
    public void should_returnValidLegalTagName_when_isNotValidTagBecomesCompliant() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", false, 0);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);

        LegalTagJobResult result = sut.run("project1", headers, "tenant");
        assertEquals(1, result.statusChangedTags.getStatusChangedTags().size());
        assertEquals(LegalTagCompliance.compliant, result.statusChangedTags.getStatusChangedTags().get(0).getChangedTagStatus());
        verify(validator, times(1)).setHeaders(any());
    }

    @Test
    public void should_returnInvalidLegalTagName_when_isValidTagBecomesIncompliant() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", true, 0);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);
        when(validator.getErrors(any())).thenReturn("Not Valid");
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(1, result.statusChangedTags.getStatusChangedTags().size());
        assertEquals(LegalTagCompliance.incompliant, result.statusChangedTags.getStatusChangedTags().get(0).getChangedTagStatus());
    }

    @Test
    public void should_sendLegalTagNameAndNewStatusWithCorrelationIdToPubSub_when_legalTagBecomesIncompliant() throws
            Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", true, 0);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);
        when(validator.getErrors(any())).thenReturn("Not Valid");

        sut.run("project1", headers, "tenant");

        verify(messagePublisherMock, times(1)).publish(any(), any(), any());
    }

    @Test
    public void should_notSendMessageToPubSub_when_noLegalTagStatusHasChanged() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();

        sut = createSut(validLegalTags);
        //when(validator.getErrors(any())).thenReturn("");

        sut.run("project1", headers, "tenant");
        verify(messagePublisherMock, never()).publish(any(), any(), any());
    }

    @Test
    public void should_notAuditLogPublishMessage_when_tagsStatusHasNotChanged() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTagStatusJob legalTagStatusJob = createSut(validLegalTags);

        legalTagStatusJob.run("project1", headers, "tenant");

        verify(auditLogger, never()).publishedStatusChangeSuccess(any());
    }

    @Test
    public void should_returnAboutToExpireLegalTagName_when_legalTagAboutToExpireIn1Year1Month2Weeks1Day() throws Exception {
        when(configMock.getExpirationAlerts()).thenReturn("1y,1m,2w,1d");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag1 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag1", true, 365);
        validLegalTags.add(aboutToExpireLegalTag1);
        LegalTag aboutToExpireLegalTag2 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag2", true, 31);
        validLegalTags.add(aboutToExpireLegalTag2);
        LegalTag aboutToExpireLegalTag3 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag3", true, 14);
        validLegalTags.add(aboutToExpireLegalTag3);
        LegalTag aboutToExpireLegalTag4 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag4", true, 1);
        validLegalTags.add(aboutToExpireLegalTag4);

        LegalTag anotherLegalTag1 = createValidLegalTagWithIsValidStatus("anotherLegalTag1", true, 2);
        validLegalTags.add(anotherLegalTag1);
        LegalTag anotherLegalTag2 = createValidLegalTagWithIsValidStatus("anotherLegalTag2", true, 30);
        validLegalTags.add(anotherLegalTag2);
        LegalTag anotherLegalTag3 = createValidLegalTagWithIsValidStatus("anotherLegalTag3", true, 100);
        validLegalTags.add(anotherLegalTag3);

        sut = createSut(validLegalTags);
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(4, result.aboutToExpireLegalTags.getLegalTags().size());
        assertEquals("aboutToExpireLegalTag1", result.aboutToExpireLegalTags.getLegalTags().get(0).getTagName());
        assertEquals("aboutToExpireLegalTag2", result.aboutToExpireLegalTags.getLegalTags().get(1).getTagName());
        assertEquals("aboutToExpireLegalTag3", result.aboutToExpireLegalTags.getLegalTags().get(2).getTagName());
        assertEquals("aboutToExpireLegalTag4", result.aboutToExpireLegalTags.getLegalTags().get(3).getTagName());
    }

    @Test
    public void should_returnAboutToExpireLegalTagName_when_legalTagAboutToExpireIn2Weeks() throws Exception {
        when(configMock.getExpirationAlerts()).thenReturn("2w");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag1 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag1", true, 13);
        validLegalTags.add(aboutToExpireLegalTag1);
        LegalTag aboutToExpireLegalTag2 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag2", true, 14);
        validLegalTags.add(aboutToExpireLegalTag2);

        LegalTag anotherLegalTag = createValidLegalTagWithIsValidStatus("anotherLegalTag", true, 20);
        validLegalTags.add(anotherLegalTag);

        sut = createSut(validLegalTags);
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(1, result.aboutToExpireLegalTags.getLegalTags().size());
        assertEquals("aboutToExpireLegalTag2", result.aboutToExpireLegalTags.getLegalTags().get(0).getTagName());
    }

    @Test
    public void should_throwException_when_wrongExpirationTime() throws Exception {
        when(configMock.getExpirationAlerts()).thenReturn("2d,XXXy");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag", true, 354);
        validLegalTags.add(aboutToExpireLegalTag);

        try {
            sut = createSut(validLegalTags);
            LegalTagJobResult result = sut.run("project1", headers, "tenant");
            fail("Should throw an exception");
        } catch (AppException e) {
            assertEquals("Invalid legal tag about to expire time value: XXXy", e.getMessage());
            assertEquals(500, e.getError().getCode());
        }
    }

    @Test
    public void should_throwException_when_wrongExpirationFormat() throws Exception {
        when(configMock.getExpirationAlerts()).thenReturn("XXX");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag", true, 354);
        validLegalTags.add(aboutToExpireLegalTag);

        try {
            sut = createSut(validLegalTags);
            LegalTagJobResult result = sut.run("project1", headers, "tenant");
            fail("Should throw an exception");
        } catch (AppException e) {
            assertEquals("Invalid legal tag about to expire time value: XXX", e.getMessage());
            assertEquals(500, e.getError().getCode());
        }
    }

    @Test
    public void should_sendLegalTagNameToPubSub_when_legalTagAboutToExpire() throws Exception {
        when(configMock.getExpirationAlerts()).thenReturn("2w");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag", true, 14);
        validLegalTags.add(aboutToExpireLegalTag);

        sut = createSut(validLegalTags);

        sut.run("project1", headers, "tenant");

        verify(aboutToExpireLegalTagPublisherMock, times(1)).publish(any(), any(), any());
    }

    @Test
    public void should_notSendLegalTagNameToPubSub_when_legalTagNotAboutToExpire() throws Exception {
        when(configMock.getExpirationAlerts()).thenReturn("2w");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag longTermLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag", true, 20);
        validLegalTags.add(longTermLegalTag);

        sut = createSut(validLegalTags);

        sut.run("project1", headers, "tenant");

        verify(aboutToExpireLegalTagPublisherMock, times(0)).publish(any(), any(), any());
    }

    private static LegalTag createValidLegalTagWithIsValidStatus(String name, Boolean isValid, int daysToExpire) {
        LegalTag legalTag = new LegalTag();
        legalTag.setName(name);
        legalTag.setIsValid(isValid);
        if (daysToExpire > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(java.util.Date.from(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cal.add(Calendar.DAY_OF_YEAR, + daysToExpire);
            Properties properties = new Properties();
            properties.setExpirationDate(new java.sql.Date(cal.getTimeInMillis()));
            legalTag.setProperties(properties);
        }
        return legalTag;
    }

    LegalTagStatusJob createSut(Collection<LegalTag> tags) throws Exception {
        when(legalTagServiceMock.listLegalTag(anyBoolean(), any())).thenReturn(tags);
        return sut;
    }
}
