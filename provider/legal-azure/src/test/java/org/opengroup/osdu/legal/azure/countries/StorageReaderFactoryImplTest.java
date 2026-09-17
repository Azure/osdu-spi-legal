package org.opengroup.osdu.legal.azure.countries;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class StorageReaderFactoryImplTest {

    @InjectMocks
    private StorageReaderFactoryImpl storageReaderFactory;

    @Mock
    private DpsHeaders dpsHeaders;

    @Mock
    private BlobStore blobStore;

    @Test
    public void shouldGetReader() {
        TenantInfo mockTenantInfo = new TenantInfo();
        String mockProjectRegion = "mockRegion";
        Mockito.when(dpsHeaders.getPartitionId()).thenReturn("test-partition-id");

        IStorageReader storageReader = storageReaderFactory.getReader(mockTenantInfo, mockProjectRegion);

        assertNotNull(storageReader);
    }
}