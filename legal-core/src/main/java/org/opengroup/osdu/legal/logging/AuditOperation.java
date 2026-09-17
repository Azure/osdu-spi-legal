//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.logging;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opengroup.osdu.legal.service.LegalServiceRole;

/**
 * Enum mapping audit operations to their required authorization roles.
 * Encapsulates role knowledge within the logging layer.
 */
public enum AuditOperation {
	CREATE_LEGAL_TAG(Arrays.asList(LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)),
	DELETE_LEGAL_TAG(Collections.singletonList(LegalServiceRole.ADMIN)),
	UPDATE_LEGAL_TAG(Arrays.asList(LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)),
	READ_LEGAL_TAG(Arrays.asList(LegalServiceRole.USER, LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)),
	READ_LEGAL_PROPERTIES(Arrays.asList(LegalServiceRole.USER, LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)),
	VALIDATE_LEGAL_TAG(Arrays.asList(LegalServiceRole.USER, LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)),
	PUBLISH_STATUS_CHANGE(Arrays.asList(LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)),
	LEGAL_TAG_STATUS_JOB(Arrays.asList(LegalServiceRole.CRON, LegalServiceRole.ADMIN)),
	LEGAL_TAG_BACKUP(Collections.singletonList(LegalServiceRole.ADMIN)),
	LEGAL_TAG_RESTORE(Collections.singletonList(LegalServiceRole.ADMIN));

	private final List<String> requiredGroups;

	AuditOperation(List<String> requiredGroups) {
		this.requiredGroups = Collections.unmodifiableList(requiredGroups);
	}

	/**
	 * Returns the list of required authorization groups for this operation.
	 * @return An unmodifiable list of required groups
	 */
	public List<String> getRequiredGroups() {
		return requiredGroups;
	}
}
