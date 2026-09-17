package org.opengroup.osdu.legal.logging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuditLoggerTests {

    private static final String TEST_USER = "test@example.com";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "TestAgent/1.0";
    private static final String TEST_AUTHORIZED_GROUP = "users.datalake.viewers";

    @Mock
    private JaxRsDpsLog log;

    @Mock
    private DpsHeaders headers;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuditLogger sut;

    private List<String> resources;
    private AuditEvents auditEvents;

    @BeforeEach
    public void setup() {
        resources = Collections.singletonList("1");

        // Create reference AuditEvents with same values mocks will produce
        auditEvents = new AuditEvents(TEST_USER, TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);

        when(headers.getUserEmail()).thenReturn(TEST_USER);
        when(headers.getUserAuthorizedGroupName()).thenReturn(TEST_AUTHORIZED_GROUP);
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
    }

    @Test
    public void should_writeLegaltagCreatedEventSuccess() {
        sut.createdLegalTagSuccess(resources);

        verify(log).audit(auditEvents.getCreateLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeLegaltagDeleteSuccessEvent() {
        sut.deletedLegalTagSuccess(resources);

        verify(log).audit(auditEvents.getDeleteLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeLegaltagJobRunSuccessEvent() {
        sut.legalTagJobRanSuccess(resources);

        verify(log).audit(auditEvents.getLegalTagStatusJobEventSuccess(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeLegalTagJobRunFailEvent() {
        sut.legalTagJobRanFail(resources);

        verify(log).audit(auditEvents.getLegalTagStatusJobEventFail(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeLegaltagPublishedStatusSuccessEvent() {
        sut.publishedStatusChangeSuccess(resources);

        verify(log).audit(auditEvents.getPublishStatusEventSuccess(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeLegalTagReadPropertiesSuccessEvent() {
        sut.readLegalPropertiesSuccess(resources);

        verify(log).audit(auditEvents.getReadLegalPropertiesEventSuccess(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeLegalTagReadPropertiesFailEvent() {
        sut.readLegalPropertiesFail(resources);

        verify(log).audit(auditEvents.getReadLegalPropertiesEventFail(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeLegalTagUpdateSuccessEvent() {
        sut.updatedLegalTagSuccess(resources);

        verify(log).audit(auditEvents.getUpdateLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeLegalTagUpdateFailEvent() {
        sut.updatedLegalTagFail(resources);

        verify(log).audit(auditEvents.getUpdateLegalTagEventFail(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeLegalTagReadSuccessEvent() {
        sut.readLegalTagSuccess(resources);

        verify(log).audit(auditEvents.getReadLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeLegalTagReadFailEvent() {
        sut.readLegalTagFail(resources);

        verify(log).audit(auditEvents.getReadLegalTagEventFail(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeLegalTagValidateSuccessEvent() {
        sut.validateLegalTagSuccess();

        verify(log).audit(auditEvents.getValidateLegalTagEventSuccess(AuditStatus.SUCCESS));
    }

    @Test
    public void should_writeLegalTagValidateFailEvent() {
        sut.validateLegalTagFail();

        verify(log).audit(auditEvents.getValidateLegalTagEventFail(AuditStatus.FAILURE));
    }

    @Test
    public void should_writeLegalTag_whenLegalTagsBackup() {
        sut.legalTagsBackup("tenant");

        verify(log).audit(auditEvents.getLegalTagBackupEvent("tenant"));
    }

    @Test
    public void should_writeLegalTag_whenLegalTagRestored() {
        sut.legalTagRestored("tenant");

        verify(log).audit(auditEvents.getLegalTagRestoreEvent("tenant"));
    }

    @Test
    public void should_useUnknownFallback_whenUserIsEmpty() {
        when(headers.getUserEmail()).thenReturn("");

        // Should not throw - uses "unknown" fallback for user
        sut.validateLegalTagSuccess();

        // Verify audit was logged with fallback values
        AuditEvents fallbackEvents = new AuditEvents("", TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);
        verify(log).audit(fallbackEvents.getValidateLegalTagEventSuccess(AuditStatus.SUCCESS));
    }
}
