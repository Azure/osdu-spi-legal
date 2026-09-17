// Copyright © SLB
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
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.util.Crc32c;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntitlementsAndCacheServiceImpl implements IEntitlementsExtensionService{

    private static final String ERROR_REASON = "Access denied";
    private static final String ERROR_MSG = "The user is not authorized to perform this action";

    @Autowired
    private IEntitlementsFactory factory;

    @Autowired
    private ICache<String, Groups> groupCache;

    @Autowired
    private JaxRsDpsLog logger;

    @Override
    public Groups getGroups(DpsHeaders headers) {
        String cacheKey = getGroupCacheKey(headers);
        Groups groups = null;
        try {
            groups = this.groupCache.get(cacheKey);
        } catch (RedisException ex) {
            this.logger.error(String.format("Error getting key %s from redis: %s", cacheKey, ex.getMessage()), ex);
        }

        if (groups == null) {
            IEntitlementsService service = this.factory.create(headers);
            try {
                groups = service.getGroups();
                this.groupCache.put(cacheKey, groups);
                this.logger.debug("Entitlements cache miss");
            } catch (EntitlementsException e) {
                HttpResponse response = e.getHttpResponse();
                this.logger.error(String.format("Error requesting entitlements service %s", response));
                throw new AppException(e.getHttpResponse().getResponseCode(), ERROR_REASON, ERROR_MSG, e);
            } catch (RedisException ex) {
                this.logger.error(String.format("Error putting key %s into redis: %s", cacheKey, ex.getMessage()), ex);
            }
        }

        return groups;
    }

    protected static String getGroupCacheKey(DpsHeaders headers) {
        String key = String.format("entitlement-groups:%s:%s", headers.getPartitionIdWithFallbackToAccountId(),
                headers.getAuthorization());
        return Crc32c.hashToBase64EncodedString(key);
    }

}
