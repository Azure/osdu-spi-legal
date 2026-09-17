package org.opengroup.osdu.legal.tags.validation;

import org.opengroup.osdu.core.common.model.legal.validation.PropertiesValidator;
import org.opengroup.osdu.legal.InvalidCooRule;
import org.opengroup.osdu.legal.MockRule;
import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesValidatorTests {

    PropertiesValidator sut;
    static ConstraintValidatorContext context;

    @BeforeAll
    public static void setupClass(){
        context = mock(ConstraintValidatorContext.class);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
    }

    @Test
    public void should_ReturnFalse_When_ContractIdIsNull_And_OriginatorIsValid(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties(null, "valid", null);
        assertFalse(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnFalse_When_ContractIdIsEmpty_And_OriginatorIsValid(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("", "valid", null);
        assertFalse(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnTrue_When_ContractIdIsNone(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties(Properties.NO_CONTRACT_ID, "", null);
        assertTrue(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnTrue_When_ContractIdIsValid_And_OriginatorIsInvalid(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties(Properties.UNKNOWN_CONTRACT_ID, "invalid!", null);
        assertTrue(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnTrue_When_ContractIdIsSet_And_OriginatorIdIsEmpty(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("ert5", "", null);
        assertTrue(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnTrue_When_ContractIdIsSetWithDotsAndDashes_And_OriginatorIdIsEmpty(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("ert.5-445", "", null);
        assertTrue(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnTrue_When_ContractIdIsSet_And_OriginatorIsSet(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("234rty", "My Company PLC 4", null);
        assertTrue(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnTrue_When_ContractIdIsUnknown_And_OriginatorIsSet(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("Unknown", "MyCompany PLC", null);
        assertTrue(sut.isValid(properties, context));
    }
    @Test
    public void should_ReturnTrue_When_ContractIdIsNoRelated_And_OriginatorIsSet(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("No Contract Related", "MyCompany PLC", null);
        assertTrue(sut.isValid(properties, context));
    }

    @Test
    public void should_returnFalse_when_expirationDateIsInThePast(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Date ts =  java.sql.Date.valueOf(LocalDate.now().minusDays(1));
        Properties properties = createProperties("ert5", "", ts);
        assertFalse(sut.isValid(properties, context));
    }
    @Test
    public void should_returnFalse_when_settingAnEmbargoedCountry(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("SY");}});
        assertFalse(sut.isValid(properties, context));
    }
    @Test
    public void should_returnTrue_when_settingAnClientConsentRequiredCountry(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("MY");}});
        assertTrue(sut.isValid(properties, context));
    }
    @Test
    public void should_returnTrue_when_3rdPartyDataAndHaveContractIdAndExpiration(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Date ts =  java.sql.Date.valueOf(LocalDate.now().plusDays(1));
        Properties properties = createProperties("abc123", "", ts);
        properties.setDataType(DataTypeValues.THIRD_PARTY_DATA);
        assertTrue(sut.isValid(properties, context));
    }

    @Test
    public void should_returnFalse_when_valid3rdPartyContractButLaterRuleFails(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Date ts =  java.sql.Date.valueOf(LocalDate.now().plusDays(1));
        Properties properties = createProperties("abc123", "", ts);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("CV");}});
        properties.setDataType(DataTypeValues.THIRD_PARTY_DATA);
        assertFalse(sut.isValid(properties, context));
    }
    @Test
    public void should_returnFalse_when_3rdPartyDataAndHaveContractId_butExpirationIsDefault(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("abc123", "", Properties.DEFAULT_EXPIRATIONDATE);
        properties.setDataType(DataTypeValues.THIRD_PARTY_DATA);
        assertFalse(sut.isValid(properties, context));
    }

    @Test
    public void should_returnFalse_when_3rdPartyDataAndExpiration_butUnknownContract(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Date ts =  java.sql.Date.valueOf(LocalDate.now().plusDays(1));
        Properties properties = createProperties(Properties.UNKNOWN_CONTRACT_ID, "", ts);
        properties.setDataType(DataTypeValues.THIRD_PARTY_DATA);
        assertFalse(sut.isValid(properties, context));
    }


    @Test
    public void should_returnTrue_when_givenUnrestrictedCountry(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("US");}});
        assertTrue(sut.isValid(properties, context));
    }
    @Test
    public void should_returnFalse_when_givenValidIsoAlpha2Code_but_hasDefaultRestrictionLevel(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("CV");}});
        assertFalse(sut.isValid(properties, context));
    }

    @Test
    public void should_ReturnTrue_When_GivenValidIsoAlpha2Code(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("US");}});
        assertTrue(sut.isValid(properties, context));
    }
    @Test
    public void should_ReturnFalse_When_GivenEmptyIsoAlpha2Code(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("");}});
        assertFalse(sut.isValid(properties, context));
    }
    @Test
    public void should_ReturnFalse_When_GivenEmptyCollection(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<>());
        assertFalse(sut.isValid(properties, context));
    }
    @Test
    public void should_ReturnFalse_When_GivenNullIsoAlpha2Code(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(null);
        assertFalse(sut.isValid(properties, context));
    }
    @Test
    public void should_ReturnFalse_When_GivenInValidIsoAlpha2Code(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("g2");}});
        assertFalse(sut.isValid(properties, context));
    }
    @Test
    public void should_ReturnFalse_When_given1EmbargoedIsoAlpha2Code(){
        sut = new PropertiesValidator(new ArrayList<>(Arrays.asList(new MockRule(), new InvalidCooRule())));
        Properties properties = createProperties("ert5", "", null);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("US"); add("SY");}});
        assertFalse(sut.isValid(properties, context));
    }

    private static Properties createProperties(String contract, String originator, Date ts) {
        if(ts == null)
            ts = Properties.DEFAULT_EXPIRATIONDATE;
        Properties properties = new Properties();
        properties.setContractId(contract);
        properties.setOriginator(originator);
        properties.setExpirationDate(ts);
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("FR");}});
        
        Map<String, Object> extensionProperties = new LinkedHashMap <String, Object>();
        extensionProperties.put("EffectiveDate", "2022-06-01T00:00:00");
        extensionProperties.put("AffiliateEnablementIndicator", true);
        Map<String, Object> agreementParty = new LinkedHashMap <String, Object>();
        agreementParty.put("AgreementPartyType", "EnabledAffiliate");
        agreementParty.put("AgreementParty", "osdu:master-data--Organisation:TestCompany");
        extensionProperties.put("AgreementParties", Arrays.asList(agreementParty));
        properties.setExtensionProperties(extensionProperties);
        
        return properties;
    }
}
