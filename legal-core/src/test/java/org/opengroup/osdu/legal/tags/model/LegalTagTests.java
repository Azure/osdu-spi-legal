package org.opengroup.osdu.legal.tags.model;

import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.MockRule;
import org.opengroup.osdu.legal.MockValidationValidator;
import org.opengroup.osdu.legal.tags.validation.rules.UnownedDataRule;

import jakarta.validation.ConstraintViolation;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


public class LegalTagTests {

    private MockValidationValidator validator = new MockValidationValidator();

    @Test
    public void should_failValidation_when_nameIsInvalid(){
        LegalTag sut = createLegalTag();
        sut.setName("my+name");
        Set<ConstraintViolation<LegalTag>> violations = validator.getValidator().validate(sut);

        assertEquals(1, violations.size());
        for (ConstraintViolation<?> violation : violations) {
            assertEquals("Invalid name given. It needs to be between 3 and 100 characters and only alphanumeric characters and hyphens allowed e.g. 'usa-public'. Found: my+name", violation.getMessage());
        }
    }

    @Test
    public void should_cascadeValidationThroughObjectGraph_when_validatingLegalTag(){
        LegalTag sut = createLegalTag();
        sut.getProperties().setDataType("wefw343y v-");

        Set<ConstraintViolation<LegalTag>> violations = validator.getValidator().validate(sut);
        assertEquals(1, violations.size()); //expect invalid properties properties to be added
    }

    @Test
    public void should_passValidation_when_givenValidLegalTag(){
        LegalTag sut = createLegalTag();

        Set<ConstraintViolation<LegalTag>> violations = validator.getValidator().validate(sut);

        assertEquals(0, violations.size());
    }

    @Test
    public void should_notReturnErrors_when_givenLegalTagIsValid(){
        LegalTag sut = createLegalTag();

        assertNull(validator.getErrors(sut));
    }

    @Test
    public void should_returnErrors_when_givenLegalTagIsNotValid(){
        LegalTag sut = createLegalTag();
        sut.getProperties().setDataType("wefw343y v-");
        assertNotNull(validator.getErrors(sut));
    }

    @Test
    public void should_returnErrors_when_givenDescriptionOver380Chars(){
        LegalTag sut = createLegalTag();
        String input = "";
        for(int i = 0; i < 382; i++){
            input += "a";
        }
        sut.setDescription(input);
        assertNotNull(validator.getErrors(sut));
    }

    @Test
    public void should_returnTrue_when_givenLegalTagIsThirdPArtyWithContractAndExprationDate(){
        MockValidationValidator validatorWithThirdPartDataRule = new MockValidationValidator(TenantInfo.ComplianceRuleSets.SHARED, MockValidationValidator.CreateRsp(new MockRule(), new UnownedDataRule()));
        LegalTag sut = createLegalTag();
        sut.getProperties().setDataType(DataTypeValues.THIRD_PARTY_DATA);
        sut.getProperties().setExpirationDate( java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        sut.getProperties().setContractId("A123345");
        assertNull(validatorWithThirdPartDataRule.getErrors(sut));
    }

    @Test
    public void should_returnFalse_when_givenLegalTagIsThirdPartyWithContractButDefaultExpirationDate(){
        MockValidationValidator validatorWithThirdPartDataRule = new MockValidationValidator(TenantInfo.ComplianceRuleSets.SHARED, MockValidationValidator.CreateRsp(new MockRule(), new UnownedDataRule()));
        LegalTag sut = createLegalTag();
        sut.getProperties().setDataType(DataTypeValues.THIRD_PARTY_DATA);
        sut.getProperties().setExpirationDate(Properties.DEFAULT_EXPIRATIONDATE);
        sut.getProperties().setContractId("A123345");
        assertNotNull(validatorWithThirdPartDataRule.getErrors(sut));
    }

    private LegalTag createLegalTag() {
        Properties properties = PropertiesTests.createValidProperties();
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("US");}});
        properties.setExpirationDate(Properties.DEFAULT_EXPIRATIONDATE);
        properties.setOriginator("OSDU");
        properties.setContractId("Unknown");
        properties.setDataType("Transferred Data");
        properties.setPersonalData("Personally Identifiable");
        properties.setSecurityClassification("Confidential");
        properties.setExportClassification("EAR99");
        
        Map<String, Object> extensionProperties = new LinkedHashMap <String, Object>();
        extensionProperties.put("EffectiveDate", "2022-06-01T00:00:00");
        extensionProperties.put("AffiliateEnablementIndicator", true);
        Map<String, Object> agreementParty = new LinkedHashMap <String, Object>();
        agreementParty.put("AgreementPartyType", "EnabledAffiliate");
        agreementParty.put("AgreementParty", "osdu:master-data--Organisation:TestCompany");
        extensionProperties.put("AgreementParties", Arrays.asList(agreementParty));
        properties.setExtensionProperties(extensionProperties);
        
        LegalTag legalTag = new LegalTag();
        legalTag.setProperties(properties);
        legalTag.setId(1L);
        legalTag.setDescription("");
        legalTag.setName("my-name-1");
        return legalTag;
    }
}
