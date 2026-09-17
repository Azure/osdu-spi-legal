package org.opengroup.osdu.legal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.jobs.LegalTagCompliance;
import org.opengroup.osdu.legal.jobs.LegalTagStatusJob;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagStatusJobControllerTest {

    public static final DpsHeaders dpsHeaders = new DpsHeaders();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RequestInfo requestInfo;

    @Mock
    private LegalTagStatusJob legalTagStatusJob;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private JaxRsDpsLog log;

    @Mock
    private ITenantFactory tenantStorageFactory;

    @InjectMocks
    private LegalTagStatusJobController sut;

    @BeforeEach
    public void setup() {

        dpsHeaders.put("data-partition-id", "common");
        when(requestInfo.getHeaders()).thenReturn(dpsHeaders);
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName("tenantName");
        tenantInfo.setProjectId("projectId");
        tenantInfo.setDataPartitionId("common");
        when(tenantStorageFactory.getTenantInfo(anyString())).thenReturn(tenantInfo);
    }

    @Test
    public void shouldReturn204_whenCheckUpdateStatus_succeeds() throws Exception {
        StatusChangedTag statusChangedTag = new StatusChangedTag("testTag", LegalTagCompliance.incompliant);
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        statusChangedTags.getStatusChangedTags().add(statusChangedTag);
        LegalTagJobResult legalTagJobResult = new LegalTagJobResult(statusChangedTags, new AboutToExpireLegalTags());
        when(legalTagStatusJob.run("projectId", dpsHeaders, "tenantName")).thenReturn(legalTagJobResult);

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(auditLogger).legalTagJobRanSuccess(any());
    }

    @Test
    public void shouldReturn500_whenCheckUpdateStatus_throwsAnError() throws Exception {
        Exception exception = new Exception("error occurred");
        when(legalTagStatusJob.run("projectId", dpsHeaders, "tenantName")).thenThrow(exception);

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        verify(log).error("Error running check LegalTag compliance job on tenant common", exception);
    }

}
