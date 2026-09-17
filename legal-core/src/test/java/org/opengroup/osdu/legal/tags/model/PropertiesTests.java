package org.opengroup.osdu.legal.tags.model;

import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.tags.validation.rules.UnownedDataRule;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.InvalidCooRule;
import org.opengroup.osdu.legal.MockRule;
import org.opengroup.osdu.legal.MockValidationValidator;

import org.opengroup.osdu.legal.tags.validation.rules.DefaultRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PropertiesTests {

    private Validator validator;

    @BeforeEach
    public void setup() {
        validator = MockValidationValidator.GetValidator();
    }

    @Test
    public void should_EnforceUpperCase_When_AssigningCountryOfOrigin(){
        Properties sut = new Properties();

        sut.setCountryOfOrigin(new ArrayList<String>(){{add("gb");}});

        assertEquals("GB", sut.getCountryOfOrigin().get(0));
    }

    @Test
    public void should_returnNull_When_AssigningNullCountryOfOrigin(){
        Properties sut = new Properties();

        sut.setCountryOfOrigin(null);

        assertEquals(null, sut.getCountryOfOrigin());
    }

    @Test
    public void should_failValidation_when_countryOfOriginIsEmbargoed(){
        Properties sut =  createValidProperties();
        runInvalidCooTest(sut, new ArrayList<String>(){{add("SD");}});
    }

    @Test
    public void should_failValidation_when_countryOfOriginIsDefault(){
        Properties sut =  createValidProperties();
        runInvalidCooTest(sut, new ArrayList<String>(){{add("VU");}});
    }
    @Test
    public void should_FailValidation_When_DataTypeIsInvalid(){
        Properties sut =  createValidProperties();
        sut.setDataType("blaaah");
        Set<ConstraintViolation<Properties>> violations = validator.validate(sut);

        assertEquals(1, violations.size());
        for (ConstraintViolation<?> violation : violations) {
            assertEquals("Invalid data type set on LegalTag properties. Found: blaaah."
                    , violation.getMessage());
        }
    }

    @Test
    public void should_FailValidation_When_ContractIDIsInvalid(){
        validator = MockValidationValidator.GetValidator(TenantInfo.ComplianceRuleSets.SHARED, MockValidationValidator.CreateRsp(new MockRule(), new DefaultRule()));
        Properties sut =  createValidProperties();
        sut.setContractId(")&^(N6");
        Set<ConstraintViolation<Properties>> violations = validator.validate(sut);

        assertEquals(1, violations.size());
        for (ConstraintViolation<?> violation : violations) {
            assertEquals("Invalid Contract Id given. Only 'Unknown' 'No Contract Related' or the real contract ID are allowed values. The contract ID must be between 3 and 40 characters and only include alphanumeric values and hyphens,"
                    , violation.getMessage());
        }
    }

    @Test
    public void should_FailValidation_When_OriginatorsInvalid(){
        Properties sut =  createValidProperties();
        sut.setOriginator(")&^(N6");
        Set<ConstraintViolation<Properties>> violations = validator.validate(sut);

        assertEquals(1, violations.size());
        for (ConstraintViolation<?> violation : violations) {
            assertEquals("Invalid Originator given. Only alphanumeric characters, whitespaces, hyphens and periods are allowed and must be between 3 and 60 characters. Found: )&^(N6", violation.getMessage());
        }
    }

    @Test
    public void should_FailValidation_When_PersonalDataInvalid(){
        Properties sut =  createValidProperties();
        sut.setPersonalData(")&^(N6");
        Set<ConstraintViolation<Properties>> violations = validator.validate(sut);

        assertEquals(1, violations.size());
        for (ConstraintViolation<?> violation : violations) {
            assertEquals("Invalid personal data value on LegalTag properties. Found: )&^(N6.", violation.getMessage());
        }
    }

    @Test
    public void should_FailValidation_When_SecurityClassificationInvalid(){
        Properties sut =  createValidProperties();
        sut.setSecurityClassification("Secret");
        Set<ConstraintViolation<Properties>> violations = validator.validate(sut);

        assertEquals(1, violations.size());
        for (ConstraintViolation<?> violation : violations) {
            assertEquals("Secret data is currently not allowed. Please do not upload this.", violation.getMessage());
        }
    }

    @Test
    public void should_FailValidation_When_ExportClassificationIsInvalid(){
        Properties sut =  createValidProperties();
        sut.setExportClassification("bad export classification");
        Set<ConstraintViolation<Properties>> violations = validator.validate(sut);

        assertEquals(1, violations.size());
        for (ConstraintViolation<?> violation : violations) {
            assertEquals("Invalid export classification on LegalTag properties. Found: bad export classification.", violation.getMessage());
        }
    }

    @Test
    public void should_PassValidation_When_AllFieldsAreValid(){
        Properties sut = createValidProperties();
        Set<ConstraintViolation<Properties>> violations = validator.validate(sut);

        assertEquals(0, violations.size());
    }

    public static Properties createValidProperties() {
        Properties sut = new Properties();
        sut.setCountryOfOrigin(new ArrayList<String>(){{add("us");}});
        sut.setOriginator("OSDU");
        sut.setContractId("Unknown");
        sut.setDataType("Transferred Data");
        sut.setPersonalData("Personally Identifiable");
        sut.setSecurityClassification("Confidential");
        sut.setExportClassification("EAR99");
        sut.setExpirationDate(Properties.DEFAULT_EXPIRATIONDATE);

        Map<String, Object> extensionProperties = new LinkedHashMap <String, Object>();
        extensionProperties.put("EffectiveDate", "2022-06-01T00:00:00");
        extensionProperties.put("AffiliateEnablementIndicator", true);
        Map<String, Object> agreementParty = new LinkedHashMap <String, Object>();
        agreementParty.put("AgreementPartyType", "EnabledAffiliate");
        agreementParty.put("AgreementParty", "osdu:master-data--Organisation:TestCompany");
        extensionProperties.put("AgreementParties", Arrays.asList(agreementParty));
        sut.setExtensionProperties(extensionProperties);
        return sut;
    }

    @Test
    public void should_returnFalse_when_givenSecondaryDataTypeWithNoContract_on_Tenant(){
        Validator validatorWithClientDataInTenantRule = MockValidationValidator.GetValidator(
                TenantInfo.ComplianceRuleSets.SHARED, MockValidationValidator.CreateRsp(new MockRule(), new UnownedDataRule()));

        Properties properties = createValidProperties();
        properties.setContractId(Properties.UNKNOWN_CONTRACT_ID);
        properties.setDataType(DataTypeValues.SECOND_PARTY_DATA);

        Set<ConstraintViolation<Properties>> violations = validatorWithClientDataInTenantRule.validate(properties);

        for (ConstraintViolation<?> violation : violations) {
            System.out.println(violation.getMessage());
        }

        assertEquals(1, violations.size());
    }

    private void runInvalidCooTest(Properties sut, ArrayList<String> coo) {
        Validator validatorWithInvalidCoo = MockValidationValidator.GetValidator(TenantInfo.ComplianceRuleSets.SHARED, MockValidationValidator.CreateRsp(new MockRule(), new InvalidCooRule()));
        sut.setCountryOfOrigin(coo);
        Set<ConstraintViolation<Properties>> violations = validatorWithInvalidCoo.validate(sut);

        for (ConstraintViolation<?> violation : violations) {
            System.out.println(violation.getMessage());
            assertTrue(violation.getMessage().startsWith("Invalid country of origin set."));
        }
        assertEquals(1, violations.size());
    }
}
