//  Copyright © SLB
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

package org.opengroup.osdu.legal.config;

import org.opengroup.osdu.core.common.cache.VmCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Value("${legal.coo.cache.expiration:60}")
    private int legalCOOCacheExpiration;
    @Value("${legal.coo.cache.maxSize:1000}")
    private int legalCOOCacheMaxSize;

    @Bean
    public VmCache<String, byte[]> legalCOOCache() {
        return new VmCache<>(legalCOOCacheExpiration * 60, legalCOOCacheMaxSize);
    }
}
