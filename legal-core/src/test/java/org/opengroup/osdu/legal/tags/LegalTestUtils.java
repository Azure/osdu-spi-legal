package org.opengroup.osdu.legal.tags;

import  java.sql.Date;
import org.opengroup.osdu.legal.tags.dto.LegalTagDto;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class LegalTestUtils {
    public static LegalTag createValidLegalTag(String name){
        LegalTag legalTag = new LegalTag();
        legalTag.setProperties(createValidProperties());
        legalTag.setName(name);
        legalTag.setIsValid(false);
        legalTag.setDefaultId();
        return legalTag;
    }
    public static Properties createValidProperties(){
        Properties properties = new Properties();
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("USA");}});
        properties.setExpirationDate(new Date(System.currentTimeMillis()));
        properties.setOriginator("MyCompany");
        properties.setContractId("Unknown");
        properties.setDataType("Tranferred Data");
        properties.setPersonalData("Sensitive Personal Information");
        properties.setSecurityClassification("Confidential");
        properties.setExportClassification("ECCN");
        
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
    public static UpdateLegalTag createUpdateLegalTag(String name){
        UpdateLegalTag legalTag = new UpdateLegalTag();
        legalTag.setExpirationDate(Properties.DEFAULT_EXPIRATIONDATE);
        legalTag.setContractId("abc123");
        legalTag.setName(name);
        legalTag.setDescription("myDescription");
        
        Map<String, Object> extensionProperties = new LinkedHashMap <String, Object>();
        extensionProperties.put("EffectiveDate", "2022-06-01T00:00:00");
        extensionProperties.put("AffiliateEnablementIndicator", false);
        Map<String, Object> agreementParty = new LinkedHashMap <String, Object>();
        agreementParty.put("AgreementPartyType", "PurchaseOrganisation");
        agreementParty.put("AgreementParty", "osdu:master-data--Organisation:TestCompany");
        extensionProperties.put("AgreementParties", Arrays.asList(agreementParty));
        
        return legalTag;
    }
    public static LegalTagDto createValidLegalTagDto(String name){
        return LegalTagDto.convertTo(createValidLegalTag(name));
    }
}
