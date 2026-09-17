package org.opengroup.osdu.legal.tags.model;

import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
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

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AllowedLegaltagPropertyValuesTests {
    @InjectMocks
    AllowedLegaltagPropertyValues sut;
    @Mock
    DataTypeValues dataTypeValues;
    @Mock
    RequestInfo requestInfo;

    @BeforeEach
    public void setUp() throws Exception {
    }
    
    @Test
    public void should_returnMyCompanyDataTypes_when_RequestInfoIsMyCompanyTenant(){
        Set<String> dts = new HashSet<String>(){{add("string");}};
        when(dataTypeValues.getDataTypeValues("MyCompany")).thenReturn(dts);

        when(requestInfo.getComplianceRuleSet()).thenReturn("MyCompany");

        assertEquals(dts, sut.getDataTypes());
    }
}
