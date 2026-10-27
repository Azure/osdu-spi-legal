/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2020-2023 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.legal.countries;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.core.obm.core.model.ObmBlob;
import org.opengroup.osdu.core.obm.core.model.ObmBucket;
import org.opengroup.osdu.core.obm.core.persistence.ObmDestination;
import org.opengroup.osdu.legal.config.PartitionPropertyNames;

@RunWith(MockitoJUnitRunner.class)
public class StorageReaderImplTests {

  private static final String TENANT_1 = "tenant1";
  private static final String FILE_NAME = "Legal_COO.json";
  private static final String BUCKET_FULL_NAME = "tenant1-tenant1-legal-config";
  private static final String CONTENT =
      """
          [{
            "name": "Malaysia",
            "alpha2": "MY",
            "numeric": 458,
            "residencyRisk": "Client consent required"
          }]""";
  private static final String PARTITION_BUCKET_NAME = "partition-bucket-name";

  @Mock
  private TenantInfo tenantInfo;

  @Mock
  private Driver storage;

  @Mock
  private PartitionPropertyNames partitionPropertyNames;

  @Mock
  private PartitionPropertyResolver partitionPropertyResolver;

  @Mock
  private ICache<String, byte[]> legalCOOCache;

  @InjectMocks
  private StorageReaderImpl sut;

  private AutoCloseable mocks;

  @BeforeEach
  void setup() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (mocks != null) {
      mocks.close();
    }
  }

  @Test
  public void should_returnAllBytes_when_bucketExistsAndFileExist() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_1);
    when(storage.getBucket(BUCKET_FULL_NAME, getDestination())).thenReturn(new ObmBucket(TENANT_1));
    when(storage.getBlob(BUCKET_FULL_NAME, FILE_NAME, getDestination()))
        .thenReturn(ObmBlob.builder().build());
    byte[] expectedBytes = CONTENT.getBytes();
    when(storage.getBlobContent(BUCKET_FULL_NAME, FILE_NAME, getDestination()))
        .thenReturn(expectedBytes);
    
    byte[] bytes = sut.readAllBytes();
    
    assertEquals(expectedBytes, bytes);
  }

  @Test
  public void should_returnEmptyArray_when_bucketNotExists() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_1);

    byte[] bytes = sut.readAllBytes();
    
    assertEquals(0, bytes.length);
  }

  @Test
  public void should_returnEmptyArray_when_FileBucketNull() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_1);
    when(storage.getBucket(BUCKET_FULL_NAME, getDestination())).thenReturn(new ObmBucket(TENANT_1));
    when(storage.getBlob(BUCKET_FULL_NAME, FILE_NAME, getDestination()))
        .thenReturn(ObmBlob.builder().build());
    when(storage.getBlobContent(BUCKET_FULL_NAME, FILE_NAME, getDestination())).thenReturn(null);

    byte[] bytes = sut.readAllBytes();
    
    assertEquals(0, bytes.length);
  }

  @Test
  public void should_returnBucketName_fromPartition() {
    when(partitionPropertyNames.getLegalBucketName()).thenReturn(PARTITION_BUCKET_NAME);
    when(partitionPropertyResolver.getOptionalPropertyValue(
            partitionPropertyNames.getLegalBucketName(), tenantInfo.getDataPartitionId()))
        .thenReturn(Optional.of(PARTITION_BUCKET_NAME));
   
    StorageReaderImpl storageReader =
        new StorageReaderImpl(
            tenantInfo, storage, partitionPropertyResolver, partitionPropertyNames, legalCOOCache);
    String resultBucketName = storageReader.getTenantBucketName();
    
    assertEquals(PARTITION_BUCKET_NAME, resultBucketName);
  }

  @Test
  public void shouldReturnContentAsBytes_whenCacheEntryFound() {
    byte[] expected = CONTENT.getBytes(StandardCharsets.UTF_8);
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_1);
    when(storage.getBucket(BUCKET_FULL_NAME, getDestination())).thenReturn(new ObmBucket(TENANT_1));
    when(legalCOOCache.get(tenantInfo.getDataPartitionId())).thenReturn(expected);

    StorageReaderImpl storageReader =
        new StorageReaderImpl(
            tenantInfo, storage, partitionPropertyResolver, partitionPropertyNames, legalCOOCache);
    byte[] actual = storageReader.readAllBytes();

    assertArrayEquals(expected, actual);
  }

  private ObmDestination getDestination() {
    return ObmDestination.builder().partitionId(TENANT_1).build();
  }
}
