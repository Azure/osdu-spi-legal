package org.opengroup.osdu.legal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.api.LegalTagStatusJobApi;
import org.opengroup.osdu.legal.jobs.LegalTagStatusJob;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;

import jakarta.inject.Inject;

import static java.util.Collections.singletonList;

@RestController
public class LegalTagStatusJobController implements LegalTagStatusJobApi {

    @Inject
    private RequestInfo requestInfo;

    @Inject
    private LegalTagStatusJob legalTagStatusJob;
    
    @Inject
    private ITenantFactory tenantStorageFactory;

    @Inject
    private AuditLogger auditLogger;

    @Inject
    private JaxRsDpsLog log;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResponseEntity<HttpStatus> checkLegalTagStatusChanges() {
        tenantStorageFactory.flushCache();
        DpsHeaders convertedHeaders = requestInfo.getHeaders();

        boolean result = runJob(convertedHeaders, tenantStorageFactory.getTenantInfo(convertedHeaders.getPartitionId()), legalTagStatusJob);

        HttpStatus status = result ? HttpStatus.NO_CONTENT : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(status);
    }

    private boolean runJob(DpsHeaders convertedHeaders, TenantInfo tenantInfo, LegalTagStatusJob legalTagStatusJob) {
        boolean success = true;
        try {
            LegalTagJobResult result = legalTagStatusJob.run(tenantInfo.getProjectId(), convertedHeaders, tenantInfo.getName());
            auditLogger.legalTagJobRanSuccess(singletonList(this.objectMapper.writeValueAsString(result)));
        } catch (Exception e) {
            success = false;
            log.error( "Error running check LegalTag compliance job on tenant " + convertedHeaders.getPartitionIdWithFallbackToAccountId(), e);
        }
        return success;
    }
}
