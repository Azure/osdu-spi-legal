package org.opengroup.osdu.legal.tags.validation.rules;

import  java.sql.Date;

import org.opengroup.osdu.legal.tags.LegalTestUtils;
import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnownedDataRuleTests {

    UnownedDataRule sut = new UnownedDataRule();

    @Test
    public void should_returnFalse_when_transferredDataType(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setDataType(DataTypeValues.TRANSFERRED_DATA);

        assertFalse(sut.shouldCheck(p));
    }
    @Test
    public void should_returnTrue_when_secondPartyDataType(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setDataType(DataTypeValues.SECOND_PARTY_DATA);

        assertTrue(sut.shouldCheck(p));
    }
    @Test
    public void should_returnTrue_when_thirdPartyDataType(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setDataType(DataTypeValues.THIRD_PARTY_DATA);

        assertTrue(sut.shouldCheck(p));
    }

    @Test
    public void should_returnError_when_noContractIdSupplied(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setContractId(Properties.UNKNOWN_CONTRACT_ID);
        p.setExpirationDate(new Date(System.currentTimeMillis()));
        assertEquals("You need to set an expiration date and contract ID.",  sut.hasError(p));
    }
    @Test
    public void should_returnError_when_defaultExpDateSupplied(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setContractId("ABC-123");
        p.setExpirationDate(Properties.DEFAULT_EXPIRATIONDATE);
        assertEquals("You need to set an expiration date and contract ID.",  sut.hasError(p));
    }
    @Test
    public void should_returnNoErrors_when_contractIdAndExpDateSupplied(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setContractId("ABC-123");
        p.setExpirationDate(new Date(System.currentTimeMillis()));
        assertEquals("", sut.hasError(p));
    }
}
