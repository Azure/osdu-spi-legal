package org.opengroup.osdu.legal.logging;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.util.IpAddressUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
@RequiredArgsConstructor
public class AuditLogger {
	private final JaxRsDpsLog logger;
	private final DpsHeaders headers;
	private final HttpServletRequest httpServletRequest;

	private AuditEvents events = null;

	private AuditEvents getEvents() {
		if (this.events == null) {
			String user = headers.getUserEmail();
			String userIpAddress = IpAddressUtil.getClientIpAddress(httpServletRequest);
			String userAgent = httpServletRequest.getHeader("User-Agent");
			String userAuthorizedGroupName = headers.getUserAuthorizedGroupName();
			this.events = new AuditEvents(user, userIpAddress, userAgent, userAuthorizedGroupName);
		}
		return this.events;
	}

	public void createdLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getCreateLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
	}

	public void deletedLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getDeleteLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
	}

	public void publishedStatusChangeSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getPublishStatusEventSuccess(AuditStatus.SUCCESS, resources));
	}

	public void readLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
	}

	public void readLegalTagFail(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalTagEventFail(AuditStatus.FAILURE, resources));
	}

	public void updatedLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getUpdateLegalTagEventSuccess(AuditStatus.SUCCESS, resources));
	}

	public void updatedLegalTagFail(List<String> resources) {
		this.writeLog(this.getEvents().getUpdateLegalTagEventFail(AuditStatus.FAILURE, resources));
	}

	public void legalTagJobRanSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getLegalTagStatusJobEventSuccess(AuditStatus.SUCCESS, resources));
	}

	public void legalTagJobRanFail(List<String> resources) {
		this.writeLog(this.getEvents().getLegalTagStatusJobEventFail(AuditStatus.FAILURE, resources));
	}

	public void readLegalPropertiesSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalPropertiesEventSuccess(AuditStatus.SUCCESS, resources));
	}

	public void readLegalPropertiesFail(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalPropertiesEventFail(AuditStatus.FAILURE, resources));
	}

	public void validateLegalTagSuccess() {
		this.writeLog(this.getEvents().getValidateLegalTagEventSuccess(AuditStatus.SUCCESS));
	}

	public void validateLegalTagFail() {
		this.writeLog(this.getEvents().getValidateLegalTagEventFail(AuditStatus.FAILURE));
	}

	public void legalTagsBackup(String tenant) {
		this.writeLog(this.getEvents().getLegalTagBackupEvent(tenant));
	}

	public void legalTagRestored(String tenant) {
		this.writeLog(this.getEvents().getLegalTagRestoreEvent(tenant));
	}

	private void writeLog(AuditPayload log) {
		this.logger.audit(log);
	}
}
