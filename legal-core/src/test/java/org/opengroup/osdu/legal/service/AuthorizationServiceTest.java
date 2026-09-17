//  Copyright © SLB
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

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationServiceTest {

    private static final String MEMBER_EMAIL = "memberEmail";

    @Mock
    private IEntitlementsExtensionService entitlementsService;

    @InjectMocks
    private AuthorizationService sut;

    @Test
    public void should_throwAppException_when_givenGroupDoesNotExistForUser() {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setName("c");
        Groups group = new Groups();
        group.setGroups(List.of(groupInfo));
        when(entitlementsService.getGroups(any())).thenReturn(group);

        assertThrows(AppException.class, () -> sut.authorizeAny(new DpsHeaders(), "a", "b"));
    }

    @Test
    public void should_returnGroupsWithUserEmail_when_givenGroupExistsForUser() {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setName("a");
        Groups group = new Groups();
        group.setGroups(List.of(groupInfo));
        group.setMemberEmail(MEMBER_EMAIL);
        when(entitlementsService.getGroups(any())).thenReturn(group);

        AuthorizationResponse response = sut.authorizeAny(new DpsHeaders(), "a", "b");

        assertEquals(MEMBER_EMAIL, response.getUser());
        assertEquals(group, response.getGroups());
    }

    @Test
    public void should_throwNotImplementedException_when_authorizeAnyWithPartitionParameter() {
        String partition = "partition";

        assertThrows(NotImplementedException.class, () -> sut.authorizeAny(partition, new DpsHeaders(), "a", "b"));
    }
}
