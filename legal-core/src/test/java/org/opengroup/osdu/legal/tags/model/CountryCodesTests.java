package org.opengroup.osdu.legal.tags.model;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.CountryCodes;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountryCodesTests {
    @Test
    public void should_ReturnCountryCode_Matching_GivenIsoCode(){
        CountryCodes result = CountryCodes.getByCode("GB");
        assertEquals(CountryCodes.GB, result);
    }
    @Test
    public void should_notReturnCountryCode_matching_givenIsoCodeInWrongCase(){
        CountryCodes result = CountryCodes.getByCode("gb");
        assertEquals(CountryCodes.Default, result);
    }
    @Test
    public void should_returnDefaultCountryCode_When_GivenNonExistantIsoCode(){
        CountryCodes result = CountryCodes.getByCode("gx");
        assertEquals(CountryCodes.Default, result);
    }
    @Test
    public void should_returnDefaultCountryCode_When_GivenNullIsoCode(){
        CountryCodes result = CountryCodes.getByCode(null);
        assertEquals(CountryCodes.Default, result);
    }

    @Test
    public void should_HaveGivenValues_Stored_InCountryCodesInstance(){
        CountryCodes result = CountryCodes.US;
        assertEquals("US", result.getAlpha2());
        assertEquals("United States", result.getName());
        assertEquals(840, result.getNumeric());
        assertEquals(CountryCodes.RESIDENCY_RISK.NO_RESTRICTION, result.getResidencyRisk());
    }

    @Test
    public void should_HaveRestrictedAccess_Unless_ExplicitlySetToNoRestriction(){
        //this test will change over time as we assign different restriction values to different countries
        List<String> unrestrictedCountries = Arrays.asList(
            "CA"
        );
        List<String> embargoed = Arrays.asList(
                "SY", "SD", "KP", "IR", "CU", "RU", "SS"
        );
        List<String> unassigned = Arrays.asList(
                "BB"
        );
        List<String> defaultList = Arrays.asList(
                "AT"
        );
        for(CountryCodes code :  CountryCodes.values()) {
            if(unrestrictedCountries.contains(code.getAlpha2()))
                assertEquals(CountryCodes.RESIDENCY_RISK.NO_RESTRICTION, code.getResidencyRisk());
            else if(embargoed.contains(code.getAlpha2()))
                assertEquals(CountryCodes.RESIDENCY_RISK.EMBARGOED, code.getResidencyRisk());
            else if(unassigned.contains(code.getAlpha2()))
                assertEquals(CountryCodes.RESIDENCY_RISK.NOT_ASSIGNED, code.getResidencyRisk());
            else if(defaultList.contains(code.getAlpha2()))
                assertEquals(CountryCodes.RESIDENCY_RISK.DEFAULT, code.getResidencyRisk());
        }
    }

    @Test
    public void should_HaveAllTHeIsoCountries_In_CountryCodesEnum(){
        assertEquals(245, CountryCodes.values().length);
    }
}
