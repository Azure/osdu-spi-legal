package org.opengroup.osdu.legal.tags.validation.rules;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.tags.LegalTestUtils;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpiredContractRuleTest {

    ExpiredContractRule sut = new ExpiredContractRule();

    @Test
    public void shouldReturnTrue_whenPropertiesHasExpired(){
        Properties properties = LegalTestUtils.createValidProperties();
        properties.setExpirationDate(new Date(System.currentTimeMillis()- 10000));
        assertTrue(sut.shouldCheck(properties));
    }

    @Test
    public void shouldReturnExpiredDateMessage(){
        Properties properties = LegalTestUtils.createValidProperties();
        properties.setExpirationDate(new Date(System.currentTimeMillis()-1000));
        String message = sut.hasError(properties);
        String expectedDate = properties.getExpirationDate().toString();
        assertEquals("Expiration date must be a value in the future. Given "+expectedDate, message);
    }

}