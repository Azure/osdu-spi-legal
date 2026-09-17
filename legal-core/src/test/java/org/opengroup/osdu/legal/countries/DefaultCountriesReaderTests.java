package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultCountriesReaderTests {

    @Test
    public void should_returnEmptyCountries_given_emptyJsonString() {
        CountriesReader sut = new DefaultCountriesReader();
        String jsonString = "";
        List<Country> countries = sut.convertJsonIntoCountries(jsonString);
        assertTrue(countries.isEmpty());
    }

    @Test
    public void should_throwAppException_when_catchIOException() {
        CountriesReader sut = new DefaultCountriesReader();
        String jsonString = getNonexistingFilePath();
        try {
            sut.convertJsonIntoCountries(jsonString);
        } catch (AppException e) {
            assertEquals(500, e.getError().getCode());
            assertEquals("Internal Server Error", e.getError().getReason());
        }

    }

    private String getNonexistingFilePath() {
        return "./src/main/DefaultCountryCode.json";
    }
}
