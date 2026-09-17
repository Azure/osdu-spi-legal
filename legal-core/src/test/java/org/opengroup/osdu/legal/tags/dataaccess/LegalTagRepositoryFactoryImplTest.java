package org.opengroup.osdu.legal.tags.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalTagRepositoryFactoryImplTest {

    @Mock
    private ITenantFactory tenantFactory;

    @Mock
    private ILegalTagRepository legalTagRepository;

    @InjectMocks
    private LegalTagRepositoryFactoryImpl sut;

    @Test
    public void shouldThrow403Exception_whenTenantNameIsNull() {
        try {
            sut.get(null);
            fail("Expected error");
        } catch (AppException appException) {
            assertEquals("Invalid tenant null", appException.getError().getMessage());
            assertEquals(403, appException.getError().getCode());
        }
    }

    @Test
    public void shouldThrow403Exception_whenTenantNotExists() {
        try {
            sut.get("noTenant");
            fail("Expected error");
        } catch (AppException appException) {
            assertEquals("Invalid tenant noTenant", appException.getError().getMessage());
            assertEquals(403, appException.getError().getCode());
        }
    }

    @Test
    public void shouldReturnLegalRepository() {
        when(tenantFactory.exists("tenant")).thenReturn(true);

        ILegalTagRepository legalTagRepository1 = sut.get("tenant");

        assertNotNull(legalTagRepository1);
    }
}