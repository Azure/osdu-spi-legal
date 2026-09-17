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

package org.opengroup.osdu.legal.azure.countries;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageReaderFactoryImpl implements IStorageReaderFactory {

    @Inject
    private DpsHeaders headers;

    @Inject
    @Named("STORAGE_CONTAINER_NAME")
    private String containerName;

    @Autowired
    private BlobStore blobStore;

    @Autowired
    private ICache<String, byte[]> legalCOOCache;

    @Override
    public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
        return new StorageReaderImpl(headers.getPartitionId(), containerName, blobStore, legalCOOCache);
    }
}
