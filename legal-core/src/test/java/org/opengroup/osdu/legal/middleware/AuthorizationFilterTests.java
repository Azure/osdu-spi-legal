package org.opengroup.osdu.legal.middleware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationFilterTests {

    private static final String ROLE1 = "role1";
    private static final String ROLE2 = "role2";
    private static final String ROLE3 = "cron.job";

    @Mock
    private DpsHeaders headers;
    @Mock
    private IAuthorizationService authorizationService;
    @InjectMocks
    private AuthorizationFilter sut;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(sut, "authorizationService", authorizationService);
    }

    @Test
    public void should_authenticateRequest_when_resourceIsRolesAllowedAnnotated() {
        final String USER_EMAIL = "test@test.com";
        final String AUTHORIZED_GROUP = "service.legal.admin";
        AuthorizationResponse authorizationResponse = AuthorizationResponse.builder()
                .user(USER_EMAIL)
                .userAuthorizedGroupName(AUTHORIZED_GROUP)
                .build();
        when(this.authorizationService.authorizeAny(any(), eq(ROLE1), eq(ROLE2))).thenReturn(authorizationResponse);

        assertTrue(this.sut.hasPermission(ROLE1, ROLE2));
        verify(headers).put(DpsHeaders.USER_EMAIL, USER_EMAIL);
        verify(headers).put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, AUTHORIZED_GROUP);
    }

    @Test
    public void should_throwAppError_when_noAuthzProvided() {
        when(this.authorizationService.authorizeAny(any(), eq(ROLE1), eq(ROLE2))).thenThrow(new AppException(403, "", ""));
        assertThrows(AppException.class, () -> this.sut.hasPermission(ROLE1, ROLE2));
    }

    @Test
    public void should_notAuthenticateRequest_when_appEngineCronHeaderIsNotAsExpectedForCronJob() {
        when(this.authorizationService.authorizeAny(any(), eq(ROLE3))).thenThrow(new AppException(403, "", ""));

        assertThrows(AppException.class, () -> this.sut.hasPermission(ROLE3));
    }
}
