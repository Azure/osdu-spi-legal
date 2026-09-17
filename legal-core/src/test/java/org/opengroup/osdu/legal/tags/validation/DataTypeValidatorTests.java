package org.opengroup.osdu.legal.tags.validation;

import org.opengroup.osdu.core.common.model.legal.validation.DataTypeValidator;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DataTypeValidatorTests {

    @Mock
    RequestInfo requestInfo;

    @InjectMocks
    DataTypeValidator sut;

    @BeforeEach
    public void setup(){
        when(requestInfo.getComplianceRuleSet()).thenReturn(TenantInfo.ComplianceRuleSets.SHARED);
    }

    @Test
    public void should_ReturnTrue_When_GivenValidDataType(){
        assertTrue(sut.isValid(DataTypeValues.FIRST_PARTY_DATA, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenEmptyDataType(){
        assertFalse(sut.isValid("", null));
    }
    @Test
    public void should_ReturnFalse_When_GivenNullDataType(){
        assertFalse(sut.isValid(null, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenInValidDataType(){
        assertFalse(sut.isValid("gb2", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenIrregularCasedValidValue(){
        assertTrue(sut.isValid("puBLIC doMAIN DATa", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenSecondaryDataAsDatatypeInSharedTenant(){
        when(requestInfo.getComplianceRuleSet()).thenReturn(TenantInfo.ComplianceRuleSets.SHARED);
        assertTrue(sut.isValid("Second Party Data", null));
    }

}
