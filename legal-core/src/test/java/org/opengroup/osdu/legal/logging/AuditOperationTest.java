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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.service.LegalServiceRole;

public class AuditOperationTest {

  @Test
  public void should_haveCorrectRolesForCreateLegalTag() {
    List<String> roles = AuditOperation.CREATE_LEGAL_TAG.getRequiredGroups();
    assertEquals(2, roles.size());
    assertTrue(roles.containsAll(Arrays.asList(LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForDeleteLegalTag() {
    List<String> roles = AuditOperation.DELETE_LEGAL_TAG.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.contains(LegalServiceRole.ADMIN));
  }

  @Test
  public void should_haveCorrectRolesForUpdateLegalTag() {
    List<String> roles = AuditOperation.UPDATE_LEGAL_TAG.getRequiredGroups();
    assertEquals(2, roles.size());
    assertTrue(roles.containsAll(Arrays.asList(LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForReadLegalTag() {
    List<String> roles = AuditOperation.READ_LEGAL_TAG.getRequiredGroups();
    assertEquals(3, roles.size());
    assertTrue(roles.containsAll(Arrays.asList(LegalServiceRole.USER, LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForReadLegalProperties() {
    List<String> roles = AuditOperation.READ_LEGAL_PROPERTIES.getRequiredGroups();
    assertEquals(3, roles.size());
    assertTrue(roles.containsAll(Arrays.asList(LegalServiceRole.USER, LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForValidateLegalTag() {
    List<String> roles = AuditOperation.VALIDATE_LEGAL_TAG.getRequiredGroups();
    assertEquals(3, roles.size());
    assertTrue(roles.containsAll(Arrays.asList(LegalServiceRole.USER, LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForPublishStatusChange() {
    List<String> roles = AuditOperation.PUBLISH_STATUS_CHANGE.getRequiredGroups();
    assertEquals(2, roles.size());
    assertTrue(roles.containsAll(Arrays.asList(LegalServiceRole.EDITOR, LegalServiceRole.ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForLegalTagStatusJob() {
    List<String> roles = AuditOperation.LEGAL_TAG_STATUS_JOB.getRequiredGroups();
    assertEquals(2, roles.size());
    assertTrue(roles.containsAll(Arrays.asList(LegalServiceRole.CRON, LegalServiceRole.ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForLegalTagBackup() {
    List<String> roles = AuditOperation.LEGAL_TAG_BACKUP.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.contains(LegalServiceRole.ADMIN));
  }

  @Test
  public void should_haveCorrectRolesForLegalTagRestore() {
    List<String> roles = AuditOperation.LEGAL_TAG_RESTORE.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.contains(LegalServiceRole.ADMIN));
  }

  @Test
  public void should_returnUnmodifiableList() {
    List<String> roles = AuditOperation.CREATE_LEGAL_TAG.getRequiredGroups();
    assertNotNull(roles);
    try {
      roles.add("should-fail");
      assertTrue(false, "Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void should_haveAllOperationsDefined() {
    for (AuditOperation op : AuditOperation.values()) {
      assertNotNull(op.getRequiredGroups());
      assertTrue(op.getRequiredGroups().size() > 0, op.name() + " should have at least one required group");
    }
  }
}
