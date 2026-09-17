package org.opengroup.osdu.legal.logging;

import static java.util.Collections.singletonList;

import java.util.List;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload.AuditPayloadBuilder;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class AuditEvents {

	private static final String UNKNOWN = "unknown";
	private static final String UNKNOWN_IP = "0.0.0.0";

	private static final String CREATE_ACTION_ID = "LG101";
	private static final String CREATE_MESSAGE_SUCCESS = "Legal tag created success";

	private static final String DELETE_ACTION_ID = "LG102";
	private static final String DELETE_MESSAGE_SUCCESS = "Legal tag deleted success";

	private static final String UPDATE_ACTION_ID = "LG103";
	private static final String UPDATE_MESSAGE_SUCCESS = "Legal tag updated success";
	private static final String UPDATE_MESSAGE_FAILURE = "Legal tag updated failure";

	private static final String RUN_JOB_ACTION_ID = "LG104";
	private static final String RUN_JOB_MESSAGE_SUCCESS = "Legal tag status job run success";
	private static final String RUN_JOB_MESSAGE_FAILURE = "Legal tag status job run failure";

	private static final String PUBLISH_ACTION_ID = "LG105";
	private static final String PUBLISH_MESSAGE_SUCCESS = "Published status changed event";

	private static final String READ_ACTION_ID = "LG106";
	private static final String READ_MESSAGE_SUCCESS = "Legal tag read success";
	private static final String READ_MESSAGE_FAILURE = "Legal tag read failure";

	private static final String LEGALTAG_BACKUP_ACTION_ID = "LG107";
	private static final String LEGALTAG_BACKUP_ACTION_MESSAGE = "LegalTags backup for tenant";

	private static final String LEGALTAG_RESTORE_ACTION_ID = "LG108";
	private static final String LEGALTAG_RESTORE_ACTION_MESSAGE = "LegalTags restored for tenant";

	private static final String PROPERTY_VALUE = "Property Values";

	private final String user;
	private final String userIpAddress;
	private final String userAgent;
	private final String userAuthorizedGroupName;

	public AuditEvents(String user, String userIpAddress, String userAgent, String userAuthorizedGroupName) {
		this.user = Strings.isNullOrEmpty(user) ? UNKNOWN : user;
		this.userIpAddress = Strings.isNullOrEmpty(userIpAddress) ? UNKNOWN_IP : userIpAddress;
		this.userAgent = Strings.isNullOrEmpty(userAgent) ? UNKNOWN : userAgent;
		this.userAuthorizedGroupName = Strings.isNullOrEmpty(userAuthorizedGroupName) ? UNKNOWN : userAuthorizedGroupName;
	}

	private AuditPayloadBuilder createAuditPayloadBuilder(List<String> requiredGroupsForAction, AuditStatus status, String actionId) {
		return AuditPayload.builder()
				.status(status)
				.user(this.user)
				.actionId(actionId)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName);
	}

	public AuditPayload getPublishStatusEventSuccess(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.PUBLISH_STATUS_CHANGE.getRequiredGroups(), status, PUBLISH_ACTION_ID)
				.action(AuditAction.PUBLISH)
				.message(PUBLISH_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalTagEventSuccess(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.READ_LEGAL_TAG.getRequiredGroups(), status, READ_ACTION_ID)
				.action(AuditAction.READ)
				.message(READ_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalTagEventFail(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.READ_LEGAL_TAG.getRequiredGroups(), status, READ_ACTION_ID)
				.action(AuditAction.READ)
				.message(READ_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalPropertiesEventSuccess(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.READ_LEGAL_PROPERTIES.getRequiredGroups(), status, READ_ACTION_ID)
				.action(AuditAction.READ)
				.message(READ_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalPropertiesEventFail(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.READ_LEGAL_PROPERTIES.getRequiredGroups(), status, READ_ACTION_ID)
				.action(AuditAction.READ)
				.message(READ_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getCreateLegalTagEventSuccess(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.CREATE_LEGAL_TAG.getRequiredGroups(), status, CREATE_ACTION_ID)
				.action(AuditAction.CREATE)
				.message(CREATE_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getDeleteLegalTagEventSuccess(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.DELETE_LEGAL_TAG.getRequiredGroups(), status, DELETE_ACTION_ID)
				.action(AuditAction.DELETE)
				.message(DELETE_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getUpdateLegalTagEventSuccess(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.UPDATE_LEGAL_TAG.getRequiredGroups(), status, UPDATE_ACTION_ID)
				.action(AuditAction.UPDATE)
				.message(UPDATE_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getUpdateLegalTagEventFail(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.UPDATE_LEGAL_TAG.getRequiredGroups(), status, UPDATE_ACTION_ID)
				.action(AuditAction.UPDATE)
				.message(UPDATE_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getLegalTagStatusJobEventSuccess(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.LEGAL_TAG_STATUS_JOB.getRequiredGroups(), status, RUN_JOB_ACTION_ID)
				.action(AuditAction.JOB_RUN)
				.message(RUN_JOB_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getLegalTagStatusJobEventFail(AuditStatus status, List<String> resources) {
		return createAuditPayloadBuilder(AuditOperation.LEGAL_TAG_STATUS_JOB.getRequiredGroups(), status, RUN_JOB_ACTION_ID)
				.action(AuditAction.JOB_RUN)
				.message(RUN_JOB_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getValidateLegalTagEventSuccess(AuditStatus status) {
		return createAuditPayloadBuilder(AuditOperation.VALIDATE_LEGAL_TAG.getRequiredGroups(), status, READ_ACTION_ID)
				.action(AuditAction.READ)
				.message(READ_MESSAGE_SUCCESS)
				.resources(singletonList(PROPERTY_VALUE))
				.build();
	}

	public AuditPayload getValidateLegalTagEventFail(AuditStatus status) {
		return createAuditPayloadBuilder(AuditOperation.VALIDATE_LEGAL_TAG.getRequiredGroups(), status, READ_ACTION_ID)
				.action(AuditAction.READ)
				.message(READ_MESSAGE_FAILURE)
				.resources(singletonList(PROPERTY_VALUE))
				.build();
	}

	public AuditPayload getLegalTagBackupEvent(String tenant) {
		return createAuditPayloadBuilder(AuditOperation.LEGAL_TAG_BACKUP.getRequiredGroups(), AuditStatus.SUCCESS, LEGALTAG_BACKUP_ACTION_ID)
				.action(AuditAction.READ)
				.message(LEGALTAG_BACKUP_ACTION_MESSAGE)
				.resources(singletonList("LegalTags backup for tenant: " + tenant))
				.build();
	}

	public AuditPayload getLegalTagRestoreEvent(String tenant) {
		return createAuditPayloadBuilder(AuditOperation.LEGAL_TAG_RESTORE.getRequiredGroups(), AuditStatus.SUCCESS, LEGALTAG_RESTORE_ACTION_ID)
				.action(AuditAction.READ)
				.message(LEGALTAG_RESTORE_ACTION_MESSAGE)
				.resources(singletonList("LegalTags restored for tenant: " + tenant))
				.build();
	}
}
