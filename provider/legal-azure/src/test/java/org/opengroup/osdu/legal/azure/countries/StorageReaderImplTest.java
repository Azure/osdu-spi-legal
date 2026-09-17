package org.opengroup.osdu.legal.azure.countries;

import com.azure.storage.blob.models.BlobStorageException;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageReaderImplTest {

    @InjectMocks
    private StorageReaderImpl storageReaderImpl;
    private final String dataPartitionId = "test-data-PartitionId";
    private final String containerName = "test-container";
    @Mock
    private BlobStore blobStore;
    @Mock
    private ICache<String, byte[]> legalCOOCache;

    private static final String LEGAL_CONFIG_FILE_NAME = "Legal_COO.json";

    private final String content = "[{\n" +
            "  \"name\": \"Malaysia\",\n" +
            "  \"alpha2\": \"MY\",\n" +
            "  \"numeric\": 458,\n" +
            "  \"residencyRisk\": \"Client consent required\"\n" +
            "}]";

    @Before
    public void setup() {
        storageReaderImpl = new StorageReaderImpl(dataPartitionId, containerName, blobStore, legalCOOCache);
    }

    @Test
    public void shouldReturnContentAsBytes_whenCountriesFileFound() {
        when(legalCOOCache.get(dataPartitionId)).thenReturn(null);
        when(blobStore.readFromStorageContainer(dataPartitionId, LEGAL_CONFIG_FILE_NAME, containerName)).thenReturn(content);

        byte[] expected = content.getBytes(StandardCharsets.UTF_8);

        byte[] actual = storageReaderImpl.readAllBytes();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldReturnContentAsBytes_whenCacheEntryFound() {
        byte[] expected = content.getBytes(StandardCharsets.UTF_8);
        when(legalCOOCache.get(dataPartitionId)).thenReturn(expected);

        byte[] actual = storageReaderImpl.readAllBytes();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldReturnEmptyBytes_whenCountriesFileNotFound() {
        BlobStorageException blobStorageException = new BlobStorageException("Blob not found", null, null);
        AppException appExceptionWithNotFoundError = new AppException(HttpStatus.SC_NOT_FOUND, "testReason", "testMessage", blobStorageException);
        when(legalCOOCache.get(dataPartitionId)).thenReturn(null);
        when(blobStore.readFromStorageContainer(dataPartitionId, LEGAL_CONFIG_FILE_NAME, containerName)).thenThrow(appExceptionWithNotFoundError);
        byte[] expected = new byte[0];

        byte[] actual = storageReaderImpl.readAllBytes();

        assertArrayEquals(expected, actual);
    }

    @Test(expected = AppException.class)
    public void shouldThrowActualAppExceptionOccurred_whenReadingCountriesFile() {
        String blobErrorMessage = "Error occurred while reading specified blob";
        BlobStorageException blobStorageException = new BlobStorageException(blobErrorMessage, null, null);
        AppException appExceptionWithNotFoundError = new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                blobErrorMessage, blobErrorMessage, blobStorageException);
        when(blobStore.readFromStorageContainer(dataPartitionId, LEGAL_CONFIG_FILE_NAME, containerName))
                .thenThrow(appExceptionWithNotFoundError);

        try {
            byte[] actual = storageReaderImpl.readAllBytes();
        } catch (AppException appException) {
            int expectedErrorCode = 500;
            validateAppException(appException, expectedErrorCode, blobErrorMessage);
            throw (appException);
        }
    }

    private void validateAppException(AppException e, int errorCode, String errorMessage) {
        assertEquals(errorCode, e.getError().getCode());
        assertThat(e.getError().getMessage(), containsString(errorMessage));
    }
}