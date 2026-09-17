package org.opengroup.osdu.legal.countries;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountryTest {

    @Test
    public void should_initialize_countrySuccessfully() {
        List<String> dataTypeAllowed = new ArrayList<>();
        dataTypeAllowed.add("Transferred Data");
        String name = "Mexico";
        String alpha2 = "MX";
        int numeric = 484;
        String residencyRisk = "No restriction";
        Country sut = new Country(name, alpha2, numeric, residencyRisk, dataTypeAllowed);

        assertEquals(name, sut.getName());
        assertEquals(alpha2, sut.getAlpha2());
        assertEquals(numeric, sut.getNumeric());
        assertEquals(residencyRisk, sut.getResidencyRisk());
        assertEquals(dataTypeAllowed, sut.getTypesNotApplyDataResidency());
    }

    @Test
    public void should_compare_country() {
        List<String> dataTypeAllowed = new ArrayList<>();
        dataTypeAllowed.add("Transferred Data");
        String name = "Mexico";
        String alpha2 = "MX";
        int numeric = 484;
        String residencyRisk = "No restriction";
        Country sut1 = new Country(name, alpha2, numeric, residencyRisk, dataTypeAllowed);
        Country sut2 = new Country(name, alpha2, numeric, residencyRisk, dataTypeAllowed);
        assertTrue(sut1.equals(sut2));
        assertTrue(sut1.canEqual(sut2));
        assertEquals(sut1.hashCode(), sut2.hashCode());
        assertEquals(sut1.toString(), sut2.toString());

    }

    @Test
    public void should_return_validTypesNotApplyDataResidencyList() {
        List<String> dataTypeAllowed = new ArrayList<>();
        dataTypeAllowed.add("Transferred Data");
        Country sut = new Country("Mexico", "MX", 484, "No restriction", dataTypeAllowed);

        assertEquals(1, sut.getTypesNotApplyDataResidency().size());
    }

    @Test
    public void should_return_EmptyTypesNotApplyDataResidencyList() {
        List<String> dataTypeAllowed = null;
        Country sut = new Country("Mexico", "MX", 484, "No restriction", dataTypeAllowed);

        assertTrue(sut.getTypesNotApplyDataResidency().isEmpty());
    }

    @Test
    public void should_verify_residency_risk() {
        assertEquals("Default", Country.RESIDENCY_RISK.DEFAULT);
        assertEquals("No restriction", Country.RESIDENCY_RISK.NO_RESTRICTION);
        assertEquals("Not assigned", Country.RESIDENCY_RISK.NOT_ASSIGNED);
        assertEquals("Embargoed", Country.RESIDENCY_RISK.EMBARGOED);
        assertEquals("Client consent required", Country.RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED);
    }
}
