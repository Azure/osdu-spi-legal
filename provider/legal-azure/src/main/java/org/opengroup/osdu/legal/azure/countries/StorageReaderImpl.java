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

import org.apache.http.HttpStatus;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StorageReaderImpl implements IStorageReader {

    private final BlobStore blobStore;

    private final String dataPartitionId;
    private final String containerName;

    private static final String LEGAL_CONFIG_FILE_NAME = "Legal_COO.json";

    private ICache<String, byte[]> legalCOOCache;

    public StorageReaderImpl(String dataPartitionId, String containerName, BlobStore blobStore, ICache<String, byte[]> legalCOOCache) {
        this.dataPartitionId = dataPartitionId;
        this.containerName = containerName;
        this.blobStore = blobStore;
        this.legalCOOCache = legalCOOCache;
    }

    @Override
    public byte[] readAllBytes() {
        try {
            byte[] legalCOO = new byte[0];
            if (Objects.isNull(legalCOOCache.get(dataPartitionId))) {
                //should return a json format of an array of Country class
                legalCOO = blobStore.readFromStorageContainer(dataPartitionId, LEGAL_CONFIG_FILE_NAME, containerName)
                        .getBytes(StandardCharsets.UTF_8);
                legalCOOCache.put(dataPartitionId, legalCOO);
            } else {
                legalCOO = legalCOOCache.get(dataPartitionId);
            }

            return legalCOO;
        } catch (AppException appException) {
            if (appException.getError().getCode() == HttpStatus.SC_NOT_FOUND) {
                // Storage File does not exist. No countries are being overwritten using Storage account.
                // Continue using the DefaultCountryCode.json
                byte[] legalCOO = new byte[0];
                legalCOOCache.put(dataPartitionId, legalCOO);
                return legalCOO;
            }
            throw appException;
        }
    }
}