package org.opengroup.osdu.legal;

public final class SwaggerDoc {

	/*
	 * Shared
	 */
	public static final String NAME_EXAMPLE = "Private-USA-EHC";

	/*
	 * LegalTag
	 */
	public static final String LEGALTAGS_TAGS = "LegalTag";
	public static final String LEGALTAGS_DESC = "";

	// create
	public static final String LEGALTAGS_CREATE_TITLE = "Create a LegalTag";
	public static final String LEGALTAGS_CREATE_NOTES = "This allows for the creation of your LegalTag. A LegalTag is uniquely identified by its name. A LegalTag must be created before you can start ingesting data and the correct LegaltTag should be assigned to that data. Allowed roles: service.legal.user, service.legal.editor, service.legal.admin";
	public static final String LEGALTAGS_CREATE_RESPONSE_OK = "Created LegalTag successfully.";
	public static final String LEGALTAGS_CREATE_RESPONSE_BADREQUEST = "Invalid parameters were given on request.";
	public static final String LEGALTAGS_CREATE_RESPONSE_CONFLICT = "A LegalTag with the given name already exists.";

	// delete
	public static final String LEGALTAGS_DELETE_TITLE = "Delete a LegalTag";
	public static final String LEGALTAGS_DELETE_NOTES = "This allows for the deletion of your LegalTag with the given name. This makes the given legaltags data invalid. Allowed roles: service.legal.admin";
	public static final String LEGALTAGS_DELETE_RESPONSE_OK = "Deleted the LegalTag successfully.";
	public static final String LEGALTAGS_DELETE_RESPONSE_BADREQUEST = "Invalid parameters were given on request.";

	// get
	public static final String LEGALTAGS_GET_TITLE = "Gets a Legaltag";
	public static final String LEGALTAGS_GET_NOTES = "This allows for the retrieval of your LegalTag using the 'name' associated with it. Allowed roles: service.legal.user, service.legal.editor, service.legal.admin";
	public static final String LEGALTAGS_GET_RESPONSE_OK = "Retrieved LegalTag successfully.";
	public static final String LEGALTAGS_GET_RESPONSE_BADREQUEST = "Invalid parameters were given on request.";
	public static final String LEGALTAGS_GET_RESPONSE_NOTFOUND = "Requested LegalTag was not found.";

	// list
	public static final String LEGALTAGS_LIST_TITLE = "List the Legaltags";
	public static final String LEGALTAGS_LIST_NOTES = "This allows for the retrieval of all LegalTags. You can specify parameters to choose what subset of LegalTags you want to list. Allowed roles: service.legal.user, service.legal.editor, service.legal.admin";
	public static final String LEGALTAGS_LIST_RESPONSE_OK = "Retrieved LegalTags successfully.";
	public static final String LEGALTAGS_LIST_RESPONSE_BADREQUEST = "Invalid parameters were given on request.";
	public static final String LEGALTAGS_LIST_PARAMETER_VALID = "If true returns only valid LegalTags, if false returns only invalid LegalTags.  Default value is true.";

	// retrieve batch
	public static final String LEGALTAGS_POST_BATCH_TITLE = "Get the Legaltags";
	public static final String LEGALTAGS_POST_BATCH_NOTES = "This allows for the retrieval of your LegalTags using the 'name' associated with it. A maximum of 25 can be retrieved at once. Allowed roles: service.legal.user, service.legal.editor, service.legal.admin";
	public static final String LEGALTAGS_POST_BATCH_RESPONSE_OK = "Retrieved LegalTags successfully.";
	public static final String LEGALTAGS_POST_BATCH_RESPONSE_BADREQUEST = "Invalid parameters were given on request.";
	public static final String LEGALTAGS_POST_BATCH_RESPONSE_NOTFOUND = "One or more requested LegalTags were not found.";

	// post validate
	public static final String LEGALTAGS_POST_VALIDATE_TITLE = "Validate the Legaltags";
	public static final String LEGALTAGS_POST_VALIDATE_NOTES = "This allows you to send the names of the LegalTags you want to validate. It will return any which are invalid and the reason they are. A maximum of 25 can be retrieved at once. Allowed roles: service.legal.user, service.legal.editor, service.legal.admin";
	public static final String LEGALTAGS_POST_VALIDATE_RESPONSE_OK = "Retrieved LegalTag names with reason successfully.";
	public static final String LEGALTAGS_POST_VALIDATE_RESPONSE_BADREQUEST = "Invalid parameters were given on request.";

	// update
	public static final String LEGALTAGS_UPDATE_TITLE = "Update a Legaltag";
	public static final String LEGALTAGS_UPDATE_NOTES = "This allows to update certain properties of your LegalTag using the 'name' associated with it. Allowed roles: service.legal.editor, service.legal.admin";
	public static final String LEGALTAGS_UPDATE_RESPONSE_OK = "Updated LegalTag successfully.";
	public static final String LEGALTAGS_UPDATE_RESPONSE_BADREQUEST = "Invalid parameters were given on request.";
	public static final String LEGALTAGS_UPDATE_RESPONSE_NOTFOUND = "Requested LegalTag to update was not found.";

	// properties
	public static final String LEGALTAGS_PROPERTIES_TITLE = "Get the allowed Legaltag values";
	public static final String LEGALTAGS_PROPERTIES_NOTES = "This allows for the retrieval of the allowed values for the given tenant for LegalTag properties when creating a LegalTag. Allowed roles: service.legal.user, service.legal.editor, service.legal.admin";
	public static final String LEGALTAGS_PROPERTIES_RESPONSE_OK = "Retrieved proeprties successfully.";

	public static final String LEGALTAGS_API_UNAUTHORIZED = "You do not have permissions to access this API.";

	/**
	 * DR
	 */
	public static final String DR_TAGS = "Disaster recovery";

	// Backup tenant-specific
	public static final String DR_BACKUP_TITLE = "Backup tenant-specific legal tags";
	public static final String DR_BACKUP_NOTES = "Trigger the backup process for the tenant-related legal tags";
	public static final String DR_BACKUP_SUCCESS = "The backup process was triggered successfully";

	// Backup all tenants
	public static final String DR_FULL_BACKUP_TITLE = "Backup all tenants legal tags";
	public static final String DR_FULL_BACKUP_NOTES = "Trigger the backup process for all tenants legal tags";
	public static final String DR_FULL_BACKUP_SUCCESS = "The backup process was triggered successfully";

	// Restore
	public static final String DR_RESTORE_TITLE = "Restore legal tags";
	public static final String DR_RESTORE_NOTES = "Restore legal tags based on a specific backup date";
	public static final String DR_RESTORE_SUCCESS = "The restore process was triggered successfully";
}
