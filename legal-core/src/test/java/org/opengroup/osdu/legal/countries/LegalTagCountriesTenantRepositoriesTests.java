package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagCountriesTenantRepositoriesTests {

    @Mock
    private IStorageReader storageReader;

    @Mock
    private IStorageReaderFactory storageReaderFactory;

    @InjectMocks
    private LegalTagCountriesTenantRepositories sut;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void should_throwAppException_when_givenBlankName(){
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName("");

        Assertions.assertThrows(AppException.class, () -> sut.get(tenantInfo, "us"));
    }

    @Test
    public void should_returnRepo_when_requestingTenantThatHasNotPreviouslyBeenRequested(){
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName("tenant1");
        when(storageReaderFactory.getReader(tenantInfo, "us")).thenReturn(storageReader);
        LegalTagCountriesRepository result = sut.get(tenantInfo, "us");
        Assertions.assertNotNull(result);
    }
}
