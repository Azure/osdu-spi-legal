package org.opengroup.osdu.legal.middleware;

import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.inject.Inject;

@Component
@RequestScope
public class AuthorizationFilter {

    @Inject
    private IAuthorizationService authorizationService;

    private DpsHeaders headers;

    @Inject
    AuthorizationFilter(DpsHeaders headers) {
        this.headers = headers;
    }

    public boolean hasPermission(String... requiredRoles) {
        AuthorizationResponse authResponse = authorizationService.authorizeAny(headers, requiredRoles);
        headers.put(DpsHeaders.USER_EMAIL, authResponse.getUser());
        headers.put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, authResponse.getUserAuthorizedGroupName());
        return true;
    }
}
