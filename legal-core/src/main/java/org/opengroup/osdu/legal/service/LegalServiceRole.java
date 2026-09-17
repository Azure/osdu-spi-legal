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

package org.opengroup.osdu.legal.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;

/**
 * Constants for legal service authorization roles and required groups for audit logging.
 */
public final class LegalServiceRole {

  // Role constants (from ServiceConfig for reference)
  public static final String ADMIN = ServiceConfig.LEGAL_ADMIN;
  public static final String EDITOR = ServiceConfig.LEGAL_EDITOR;
  public static final String USER = ServiceConfig.LEGAL_USER;
  public static final String CRON = ServiceConfig.LEGAL_CRON;

  // Required groups for different operations
  public static final List<String> OPS_ADMIN_ONLY = Collections.singletonList(ADMIN);
  public static final List<String> OPS_EDITORS = Arrays.asList(EDITOR, ADMIN);
  public static final List<String> OPS_READERS = Arrays.asList(USER, EDITOR, ADMIN);
  public static final List<String> OPS_CRON = Arrays.asList(CRON, ADMIN);

  private LegalServiceRole() {
    // Utility class
  }
}
