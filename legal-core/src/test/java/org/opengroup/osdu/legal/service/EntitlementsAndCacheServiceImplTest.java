// Copyright Â© SLB
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.service;

import io.lettuce.core.RedisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntitlementsAndCacheServiceImplTest {

    private static final String HEADER_ACCOUNT_ID = "anyTenant";
    private static final String HEADER_AUTHORIZATION = "anyToken";
    private static final Map<String, String> headerMap = new HashMap<>();
    private static final String EXCEPTION_MESSAGE = "exception message";
    private static final String ERROR_REASON = "Access denied";
    private static final String ERROR_MSG = "The user is not authorized to perform this action";
    private static final String RATE_LIMIT_REASON = "Too many requests";
    private static final String RATE_LIMIT_MSG = "The entitlements service rate limit was exceeded";
    private static final String UNAVAILABLE_REASON = "Service unavailable";
    private static final String UNAVAILABLE_MSG = "The entitlements service is unavailable";

    @Mock
    private IEntitlementsFactory entitlementFactory;

    @Mock
    private ICache<String, Groups> cache;

    @Mock
    private IEntitlementsService entitlementService;

    @Mock
    private JaxRsDpsLog logger;

    @InjectMocks
    private EntitlementsAndCacheServiceImpl sut;

    private DpsHeaders headers;

    @BeforeEach
    public void setup() {
        setDefaultHeaders();
        headers = DpsHeaders.createFromMap(headerMap);
    }

    @Test
    public void should_returnGroups_whenGroupsAreInCache() {
        Groups groups = new Groups();
        when(cache.get(anyString())).thenReturn(groups);

        Groups result = sut.getGroups(headers);

        assertEquals(result, groups);
        verify(cache, never()).put(anyString(), any(Groups.class));
    }

    @Test
    public void should_returnGroups_whenGroupsAreNotInCache() throws EntitlementsException {
        Groups groups = new Groups();
        when(cache.get(anyString())).thenReturn(null);
        when(this.entitlementFactory.create(headers)).thenReturn(entitlementService);
        when(entitlementService.getGroups()).thenReturn(groups);

        Groups result = sut.getGroups(headers);

        assertEquals(result, groups);
        verify(cache).get(anyString());
        verify(cache).put(anyString(), any(Groups.class));
    }

    @Test
    public void should_logError_whenCatchRedisException_whenGetCache() throws EntitlementsException {
        Groups groups = new Groups();
        String cacheKey = EntitlementsAndCacheServiceImpl.getGroupCacheKey(headers);
        RedisException ex = new RedisException(EXCEPTION_MESSAGE);
        when(cache.get(anyString())).thenThrow(ex);
        when(this.entitlementFactory.create(headers)).thenReturn(entitlementService);
        when(entitlementService.getGroups()).thenReturn(groups);

        sut.getGroups(headers);

        verify(logger).error(String.format("Error getting key %s from redis: %s", cacheKey, ex.getMessage()), ex);
    }

    @Test
    public void should_passThrough401_whenEntitlementsReturnsUnauthorized() throws EntitlementsException {
        HttpResponse httpResponse = responseWithCode(401);
        AppException exception = assertThrows(AppException.class, () -> getGroupsThrowing(httpResponse));

        verify(logger).error(String.format("Error requesting entitlements service %s", httpResponse));
        assertEquals(401, exception.getError().getCode());
        assertEquals(ERROR_REASON, exception.getError().getReason());
        assertEquals(ERROR_MSG, exception.getError().getMessage());
    }

    @Test
    public void should_passThrough403_whenEntitlementsReturnsForbidden() throws EntitlementsException {
        AppException exception = assertThrows(AppException.class, () -> getGroupsThrowing(responseWithCode(403)));

        assertEquals(403, exception.getError().getCode());
        assertEquals(ERROR_REASON, exception.getError().getReason());
        assertEquals(ERROR_MSG, exception.getError().getMessage());
    }

    @Test
    public void should_passThrough429_whenEntitlementsReturnsRateLimit() throws EntitlementsException {
        AppException exception = assertThrows(AppException.class, () -> getGroupsThrowing(responseWithCode(429)));

        assertEquals(429, exception.getError().getCode());
        assertEquals(RATE_LIMIT_REASON, exception.getError().getReason());
        assertEquals(RATE_LIMIT_MSG, exception.getError().getMessage());
    }

    @Test
    public void should_throw401_whenEntitlementsReturnsOtherClientErrors() throws EntitlementsException {
        AppException badRequest = assertThrows(AppException.class, () -> getGroupsThrowing(responseWithCode(400)));
        assertEquals(401, badRequest.getError().getCode());

        AppException notFound = assertThrows(AppException.class, () -> getGroupsThrowing(responseWithCode(404)));
        assertEquals(401, notFound.getError().getCode());
    }

    @Test
    public void should_throw503_whenEntitlementsReturnsServerError() throws EntitlementsException {
        HttpResponse httpResponse = responseWithCode(503);
        AppException exception = assertThrows(AppException.class, () -> getGroupsThrowing(httpResponse));

        verify(logger).error(String.format("Error requesting entitlements service %s", httpResponse));
        assertEquals(503, exception.getError().getCode());
        assertEquals(UNAVAILABLE_REASON, exception.getError().getReason());
        assertEquals(UNAVAILABLE_MSG, exception.getError().getMessage());
    }

    @Test
    public void should_throw503_whenEntitlementsTimesOutOrHasNoStatus() throws EntitlementsException {
        AppException timeout = assertThrows(AppException.class, () -> getGroupsThrowing(responseWithCode(0)));
        assertEquals(503, timeout.getError().getCode());

        AppException serverError = assertThrows(AppException.class, () -> getGroupsThrowing(responseWithCode(500)));
        assertEquals(503, serverError.getError().getCode());
    }

    @Test
    public void should_logError_whenCatchRedisException_whenPutToCache() throws EntitlementsException {
        Groups groups = new Groups();
        String cacheKey = EntitlementsAndCacheServiceImpl.getGroupCacheKey(headers);
        when(cache.get(anyString())).thenReturn(null);
        when(this.entitlementFactory.create(headers)).thenReturn(entitlementService);
        RedisException ex = new RedisException(EXCEPTION_MESSAGE);
        when(entitlementService.getGroups()).thenReturn(groups);
        doThrow(ex).when(cache).put(anyString(), any(Groups.class));

        sut.getGroups(headers);

        verify(logger).error(String.format("Error putting key %s into redis: %s", cacheKey, ex.getMessage()), ex);
    }

    private void setDefaultHeaders() {
        headerMap.put(DpsHeaders.ACCOUNT_ID, HEADER_ACCOUNT_ID);
        headerMap.put(DpsHeaders.AUTHORIZATION, HEADER_AUTHORIZATION);
    }

    private void getGroupsThrowing(HttpResponse httpResponse) throws EntitlementsException {
        when(cache.get(anyString())).thenReturn(null);
        EntitlementsException ex = new EntitlementsException(EXCEPTION_MESSAGE, httpResponse);
        when(this.entitlementFactory.create(headers)).thenReturn(entitlementService);
        // doThrow: when().thenThrow() invokes getGroups() and rethrows a previous stub.
        doThrow(ex).when(entitlementService).getGroups();
        sut.getGroups(headers);
    }

    private static HttpResponse responseWithCode(int status) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseCode(status);
        return httpResponse;
    }

}
